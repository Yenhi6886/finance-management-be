package com.example.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicController {

    @GetMapping("/public/oauth2-success")
    public String oauth2Success() {
        return "Login successful! You can close this window.";
    }
}
