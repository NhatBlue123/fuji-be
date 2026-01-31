package com.example.fuji.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fuji.dto.AuthRequest;
import com.example.fuji.dto.AuthResponse;
import com.example.fuji.dto.RegisterRequest;
import com.example.fuji.entity.Otp;
import com.example.fuji.entity.User;
import com.example.fuji.repository.OtpRepository;
import com.example.fuji.repository.UserRepository;
import com.example.fuji.utils.JwtUtils;

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
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;

    @Transactional
    public String register(RegisterRequest request) {
        System.out.println("Registering user: " + request.getUsername() + ", Email: " + request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại!");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
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

    public AuthResponse login(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (!user.getIsActive()) {
            throw new RuntimeException("Tài khoản chưa được kích hoạt qua OTP!");
        }

        String jwt = jwtUtils.generateTokenFromUsername(user.getUsername());

        return new AuthResponse(jwt, user.getUsername(), user.getEmail());
    }

}
