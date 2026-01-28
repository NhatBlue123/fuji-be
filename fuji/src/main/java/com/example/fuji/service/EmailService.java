package com.example.fuji.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("nguyennhat082004@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Mã xác thực đăng ký (OTP) Fuji");
        message.setText("Xin chào,\n\nMã xác thực của bạn là: " + otpCode + "\n\nMã này sẽ hết hạn trong 5 phút.");

        mailSender.send(message);
        System.out.println("Mail sent to " + toEmail);
    }

}