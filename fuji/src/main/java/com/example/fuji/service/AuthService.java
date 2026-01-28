package com.example.fuji.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fuji.dto.RegisterRequest;
import com.example.fuji.entity.Otp;
import com.example.fuji.entity.User;
import com.example.fuji.repository.OtpRepository;
import com.example.fuji.repository.UserRepository;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OtpRepository otpRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    @Transactional
    public String register(RegisterRequest request){
        System.out.println("Registering user: " + request.getUsername() + ", Email: " + request.getEmail());
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email đã tồn tại!");
        }
        if(userRepository.existsByUsername(request.getUsername())){
            throw new RuntimeException("Username đã tồn tại!");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        user.setIsActive(false);

        String otpCode = String.valueOf(new Random().nextInt(900000) + 100000);
        Otp otp = new Otp();
        otp.setEmail(request.getEmail());
        otp.setOtpCode(otpCode);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otp);
        emailService.sendOtpEmail(request.getEmail(), otpCode);

        return "Đăng ký thành công. Vui lòng kiểm tra email để nhận OTP.";
    }

    @Transactional
public String verifyOtp(String email, String code) {
    Otp otp = otpRepository.findByEmailAndOtpCode(email, code)
            .orElseThrow(() -> new RuntimeException("Mã OTP không chính xác!"));

    if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
        otpRepository.delete(otp);
        throw new RuntimeException("Mã OTP đã hết hạn!");
    }

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
    
    user.setIsActive(true); 
    userRepository.save(user);

    otpRepository.delete(otp);

    return "Xác thực thành công! Bạn hiện có thể đăng nhập.";
}

}