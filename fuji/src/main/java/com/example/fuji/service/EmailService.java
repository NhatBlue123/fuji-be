package com.example.fuji.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @org.springframework.scheduling.annotation.Async
    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("nguyennhat082004@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Mã xác thực (OTP) Fuji");
            message.setText("Xin chào,\n\nMã xác thực của bạn là: " + otpCode + "\n\nMã này sẽ hết hạn trong 5 phút.");

            mailSender.send(message);
            System.out.println("Mail sent to " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
            // We catch but don't rethrow to avoid breaking the main authentication flow
        }
    }

}