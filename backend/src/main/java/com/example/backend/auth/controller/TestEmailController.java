package com.example.backend.auth.controller;

import com.example.backend.util.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test-email")
@RequiredArgsConstructor
public class TestEmailController {

    private final EmailService emailService;

    @GetMapping
    public String testEmail() {
        emailService.sendEmail("elton2001.em@gmail.com", "Test Email", "This is a test email.");
        return "Email sent!";
    }
}
