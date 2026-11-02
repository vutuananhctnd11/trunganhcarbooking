package com.example.trunganhcar.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/call")
public class CallRedirectController {

    /**
     * Endpoint chuyển hướng cuộc gọi tel:{phone}
     * Giúp vượt qua giới hạn của Telegram Inline Keyboard chỉ cho phép giao thức http/https
     */
    @GetMapping("/{phone}")
    public ResponseEntity<Void> redirectToPhoneCall(@PathVariable String phone) {
        String cleanPhone = phone.replaceAll("[^0-9+]", "");
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("tel:" + cleanPhone));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}

