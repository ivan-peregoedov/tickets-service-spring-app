package com.pepeg.application.controller;

import org.springframework.web.bind.annotation.RestController;

import com.pepeg.application.dto.AuthResponse;
import com.pepeg.application.entity.User;
import com.pepeg.application.service.AuthService;
import com.pepeg.application.service.OrderService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@CrossOrigin
public class UserController {
    private final AuthService authService;
    private final OrderService orderService;

    @PostMapping("register")
    public ResponseEntity<AuthResponse> register(@RequestBody User request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("login")
    public ResponseEntity<AuthResponse> login(@RequestBody User request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @GetMapping("entry")
    public String getDecoded(@RequestParam String encodedMessage) throws Exception {

        return orderService.decrypting(encodedMessage);
    }

    
}
