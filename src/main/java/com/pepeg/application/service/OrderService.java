package com.pepeg.application.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.awt.image.BufferedImage;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import com.pepeg.application.entity.Order;
import com.pepeg.application.repository.OrderRepository;
import com.pepeg.application.repository.TicketRepository;
import com.pepeg.application.repository.UserRepository;
import com.spire.barcode.BarCodeGenerator;
import com.spire.barcode.BarCodeType;
import com.spire.barcode.QRCodeECL;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.persistence.EntityNotFoundException;

import com.spire.barcode.BarcodeSettings;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    private String secretKeyString = "D9BfB2fQ18dV3HjWVsAmMqK8hcbtKuf8";

    private SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(secretKeyString), 0,
            Base64.getDecoder().decode(secretKeyString).length, "AES");

    public Order createOrder(Order order, String userEmail, Long ticketId) throws Exception {
        order.setUser(userRepository.findByEmail(userEmail).get());
        order.setTicket(ticketRepository.findById(ticketId).get());
        order.setOperationDateTime(LocalDateTime.now());
        order.setStatus("Ожидает оплаты");

        return orderRepository.save(order);
    }

    public String pay(String id) throws Exception {
        Order order = orderRepository.findById(id).get();
        String idKey = id;
        int price = order.getTicket().getPrice();
        String description = order.getTicket().getEvent().getDescription();
        try {
            URL url = new URL("https://api.yookassa.ru/v3/payments");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder()
                    .encodeToString("396680:test_TtwjgjtovcypndR22sy3AaQUyMHfFaH_1HaTFcRasMw".getBytes()));

            String postData = "{\"amount\": {\"value\": \"%s\", \"currency\": \"RUB\"}, \"confirmation\": {\"type\": \"embedded\"}, \"capture\": true, \"description\": \"%s\"}"
                    .formatted(price, description);

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Idempotence-Key", "%s".formatted(idKey));
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = postData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            String confirmToken = null;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        if (line.contains("\"confirmation_token\"")) {
                            confirmToken = line.substring(line.indexOf(":") + 3, line.length() - 1);
                            connection.disconnect();
                            return confirmToken;
                        }
                    }
                }

            }
            connection.disconnect();
            return confirmToken;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public Order confirm(String orderId) {
        Order existingOrder = orderRepository.findById(orderId).get();

        existingOrder.setStatus("Оплачено");

        int count = existingOrder.getTicket().getCount().intValue();
        existingOrder.getTicket().setCount(count - 1);

        return orderRepository.save(existingOrder);
    }

    private String encrypting(Order order) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {

        String secretMessage = order.getId() + " " + order.getUser().getEmail() + " " +
                order.getTicket().getEvent().getName() + " " + order.getTicket().getType() + " "
                + order.getTicket().getEvent().getCity()
                + " " + order.getTicket().getEvent().getStartDate().toString().replace('T', ' ');

        Cipher cipher = Cipher.getInstance("AES");

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptedMessage = cipher.doFinal(secretMessage.getBytes());

        String encodedMessage = Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedMessage);

        return encodedMessage;
    }

    public void createQr(String id) throws Exception {
        Order order = orderRepository.findById(id).get();
        String encodedMessage = encrypting(order);
        BarcodeSettings settings = new BarcodeSettings();
        settings.setType(BarCodeType.QR_Code);
        String data = encodedMessage;
        System.out.println(data);
        settings.setData("http://192.168.0.13:9090/entry?encodedMessage=" + data);
        settings.setX(3);
        settings.setQRCodeECL(QRCodeECL.M);

        settings.setShowText(false);
        settings.setShowTopText(true);
        settings.setShowBottomText(true);

        settings.hasBorder(false);

        BarCodeGenerator barCodeGenerator = new BarCodeGenerator(settings);

        BufferedImage bufferedImage = barCodeGenerator.generateImage();

        ImageIO.write(bufferedImage, "png", new File("QR_Code.png"));

        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.yandex.ru");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.tls.trust", "smtp.mailtrap.io");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("peregoedov21@yandex.ru", "vrincimtvxnknzye");
            }
        });

        String filePath = "QR_Code.png";

        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress("peregoedov21@yandex.ru"));
            message.setRecipients(
                    Message.RecipientType.TO, InternetAddress.parse(order.getUser().getEmail()));
            message.setSubject("QR-код для прохода на мероприятие: " + order.getTicket().getEvent().getName());

            MimeMultipart multipart = new MimeMultipart();

            MimeBodyPart imageBodyPart = new MimeBodyPart();
            MimeBodyPart texBodyPart = new MimeBodyPart();

            String msg = "QR-код для прохода на мероприятие: " + order.getTicket().getEvent().getName() + "\n\n"
                    + "Номер билета: " + order.getId() + "\n" + "Место проведения: "
                    + order.getTicket().getEvent().getCity() + ", " + order.getTicket().getEvent().getLocation() + "\n"
                    + "Дата проведения: " + order.getTicket().getEvent().getStartDate().toString().replace('T', ' ')
                    + "\n\n"
                    + "Спасибо за покупку!";

            texBodyPart.setText(msg);
            imageBodyPart.attachFile(new File(filePath));

            multipart.addBodyPart(texBodyPart);
            multipart.addBodyPart(imageBodyPart);

            message.setContent(multipart);

            Transport.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String decrypting(String encodedMessage)
            throws Exception {

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getUrlDecoder().decode(encodedMessage));
        String decryptedMessage = new String(decryptedBytes);

        String orderId = decryptedMessage.substring(0, decryptedMessage.indexOf(" "));
        Order order = orderRepository.findById(orderId).get();
        if (orderId.equals(order.getId()) && (order.getStatus().equals("Оплачено"))) {
            order.setStatus("Использован");
            return decryptedMessage;
        }

        return "Билет уже использован или не был оплачен";
    }

}

// curl https:// api.yookassa.ru/v3/payments \
// -
// X POST\-u
// 396680:test_TtwjgjtovcypndR22sy3AaQUyMHfFaH_1HaTFcRasMw\-H'Idempotence-Key:nghyuerubn'
// \-H'Content-Type:application/json' \-d'{"amount":
// {
// "value": "2.00",
// "currency": "RUB"
// },"confirmation":
// {
// "type": "embedded"
// },"capture":true,"description":"Заказ №72"
// }
// '
