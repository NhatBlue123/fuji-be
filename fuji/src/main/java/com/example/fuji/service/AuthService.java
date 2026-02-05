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

    @Transactional
    public String register(RegisterDTO request) {
        log.info("Registering user: {}, Email: {}", request.getUsername(), request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email đã tồn tại!");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username đã tồn tại!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(false);
        userRepository.save(user);

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
                .orElseThrow(() -> new ResourceNotFoundException("Mã OTP không chính xác!"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpRepository.delete(otp);
            throw new UnauthorizedException("Mã OTP đã hết hạn!");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));

        user.setIsActive(true);
        userRepository.save(user);
        otpRepository.delete(otp);

        return "Xác thực thành công! Bạn hiện có thể đăng nhập.";
    }

    public AuthResponse login(AuthDTO authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Tài khoản chưa được kích hoạt qua OTP!");
        }

        String accessToken = jwtUtils.generateTokenFromUsername(user.getUsername(), user.getId());
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        return new AuthResponse(accessToken, refreshToken, user.getUsername(), user.getEmail());
    }

    @Transactional
    public AuthResponse refreshAccessToken(String refreshToken) {
        var newRefreshToken = refreshTokenService.verifyAndRotate(refreshToken);
        User user = newRefreshToken.getUser();

        String accessToken = jwtUtils.generateTokenFromUsername(user.getUsername(), user.getId());
        return new AuthResponse(accessToken, newRefreshToken.getToken(), user.getUsername(), user.getEmail());
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenService.revokeAllUserTokens(userId);
    }
}
