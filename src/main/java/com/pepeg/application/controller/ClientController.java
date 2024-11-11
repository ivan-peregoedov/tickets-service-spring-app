package com.pepeg.application.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pepeg.application.entity.Event;
import com.pepeg.application.entity.Order;
import com.pepeg.application.service.EventService;
import com.pepeg.application.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/client")
@CrossOrigin
@RequiredArgsConstructor
public class ClientController {
    private final EventService eventService;

    private final OrderService orderService;

    @GetMapping("events")
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("events/{city}")
    public List<Event> getEventsByCity(@PathVariable String city) {
        return eventService.getEventsByCity(city);
    }

    @PostMapping("events")
    public Order createOrder(Order order, @RequestParam Long ticketId, Principal principal) throws Exception {
        return orderService.createOrder(order, principal.getName(), ticketId);
    }

    @PostMapping("events/pay")
    public String pay(@RequestParam String id) {
        try {
            return orderService.pay(id);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @PostMapping("events/confirm")
    public Order confirm(@RequestParam String id) {
        return orderService.confirm(id);
    }

    @PostMapping("events/generateQr")
    public void createQr(@RequestParam String id) throws Exception {
        orderService.createQr(id);
    }

}
