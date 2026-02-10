package com.example.fuji.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fuji.dto.request.AuthDTO;
import com.example.fuji.dto.request.RegisterDTO;
import com.example.fuji.dto.request.SendOtpRegisterDTO;
import com.example.fuji.dto.response.AuthResponse;
import com.example.fuji.entity.Otp;
import com.example.fuji.entity.User;
import com.example.fuji.exception.ConflictException;
import com.example.fuji.exception.ResourceNotFoundException;
import com.example.fuji.exception.UnauthorizedException;
import com.example.fuji.repository.OtpRepository;
import com.example.fuji.repository.UserRepository;
import com.example.fuji.utils.JwtUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    /**
     * Gửi OTP cho đăng ký - validate đầy đủ thông tin trước khi gửi
     * KHÔNG tạo user, KHÔNG lưu thông tin đăng ký
     */
    @Transactional
    public void sendOtpForRegistration(SendOtpRegisterDTO request) {
        log.info("📧 Gửi OTP đăng ký cho email: {}", request.getEmail());

        // Check email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email đã tồn tại!");
        }

        // Check username đã tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Tên đăng nhập đã tồn tại!");
        }

        // Tạo và gửi OTP
        String otpCode = String.valueOf(new Random().nextInt(900000) + 100000);
        Otp otp = new Otp();
        otp.setEmail(request.getEmail());
        otp.setOtpCode(otpCode);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otp);

        emailService.sendOtpEmail(request.getEmail(), otpCode);
    }

    /**
     * Đăng ký tài khoản - nhận đầy đủ thông tin + OTP
     * Verify OTP trước → validate thông tin → tạo user (active ngay)
     */
    @Transactional
    public String register(RegisterDTO request) {
        log.info("Đăng ký user: {}, Email: {}", request.getUsername(), request.getEmail());

        // 1. Verify OTP trước
        Otp otp = otpRepository.findByEmailAndOtpCode(request.getEmail(), request.getOtpCode())
                .orElseThrow(() -> new UnauthorizedException("Mã OTP không chính xác!"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpRepository.delete(otp);
            throw new UnauthorizedException("Mã OTP đã hết hạn!");
        }

        // 2. Validate thông tin
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email đã tồn tại!");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username đã tồn tại!");
        }

        // 3. Tạo user - active ngay vì đã verify OTP
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);
        userRepository.save(user);

        // 4. Xóa OTP đã sử dụng
        otpRepository.delete(otp);

        log.info("🎉 Đăng ký thành công cho user: {}", request.getUsername());
        return "Đăng ký thành công! Bạn có thể đăng nhập ngay.";
    }

    /**
     * Xác thực OTP (dùng cho verify email, forgot password, v.v.)
     */
    @Transactional
    public String verifyOtp(String email, String otpCode) {
        Otp otp = otpRepository.findByEmailAndOtpCode(email, otpCode)
                .orElseThrow(() -> new UnauthorizedException("Mã OTP không chính xác!"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpRepository.delete(otp);
            throw new UnauthorizedException("Mã OTP đã hết hạn!");
        }

        otpRepository.delete(otp);
        return "Xác thực OTP thành công!";
    }

    public AuthResponse login(AuthDTO authRequest) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user and verify active status
        User user = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Tài khoản chưa được kích hoạt!");
        }

        // Generate tokens
        String accessToken = jwtUtils.generateTokenFromUsername(user.getUsername(), user.getId());
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        return new AuthResponse(accessToken, refreshToken, user.getUsername());
    }

    @Transactional
    public AuthResponse refreshAccessToken(String refreshToken) {
        var newRefreshToken = refreshTokenService.verifyAndRotate(refreshToken);
        User user = newRefreshToken.getUser();

        String accessToken = jwtUtils.generateTokenFromUsername(user.getUsername(), user.getId());
        return new AuthResponse(accessToken, newRefreshToken.getToken(), user.getUsername());
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenService.revokeAllUserTokens(userId);
    }
}
