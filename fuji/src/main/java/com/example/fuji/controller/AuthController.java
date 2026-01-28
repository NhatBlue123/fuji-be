package com.example.fuji.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fuji.dto.RegisterRequest;
import com.example.fuji.dto.VerifyOtpRequest;
import com.example.fuji.service.AuthService; // ĐÚNG


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            String message = authService.register(request);
            return ResponseEntity.ok(message); 
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); 
        }
    }

    @PostMapping("/verify-otp")
public ResponseEntity<?> verify(@RequestBody VerifyOtpRequest request) {
    try {
        String result = authService.verifyOtp(request.getEmail(), request.getOtpCode());
        return ResponseEntity.ok(result);
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}

}