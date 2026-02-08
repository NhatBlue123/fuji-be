package com.example.fuji.service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

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
import com.example.fuji.entity.UserSession;
import com.example.fuji.exception.ConflictException;
import com.example.fuji.exception.ResourceNotFoundException;
import com.example.fuji.exception.UnauthorizedException;
import com.example.fuji.repository.OtpRepository;
import com.example.fuji.repository.UserRepository;
import com.example.fuji.repository.UserSessionRepository;
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
    private final UserSessionRepository userSessionRepository;
    private final com.example.fuji.repository.OAuth2PendingSessionRepository oauth2PendingSessionRepository;

    @Transactional
    public com.example.fuji.dto.response.OAuth2LoginResult processOAuth2Login(String email, String googleId,
            String name) {
        log.info("Processing OAuth2 Login for: {}", email);

        // Check if user exists and is active
        java.util.Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent() && existingUser.get().getIsActive()) {
            User user = existingUser.get();
            // Update googleId if not set
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                userRepository.save(user);
            }
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            log.info("Existing active user found for {}, skipping OTP", email);
            return com.example.fuji.dto.response.OAuth2LoginResult.builder()
                    .authResponse(generateAuthResponse(user))
                    .needsOtp(false)
                    .email(email)
                    .build();
        }

        // Create pending session for new or inactive users
        com.example.fuji.entity.OAuth2PendingSession session = new com.example.fuji.entity.OAuth2PendingSession();
        session.setEmail(email);
        session.setGoogleId(googleId);
        session.setFullName(name);
        session.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        oauth2PendingSessionRepository.save(session);

        // Create OTP
        String otpCode = String.valueOf(new Random().nextInt(900000) + 100000);
        Otp otp = new Otp();
        otp.setEmail(email);
        otp.setOtpCode(otpCode);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otp);

        // Send Email
        emailService.sendOtpEmail(email, otpCode);

        return com.example.fuji.dto.response.OAuth2LoginResult.builder()
                .sessionId(session.getSessionId())
                .needsOtp(true)
                .email(email)
                .build();
    }

    @Transactional
    public AuthResponse verifyOAuth2Otp(String sessionId, String otpCode) {
        com.example.fuji.entity.OAuth2PendingSession pendingSession = oauth2PendingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Phiên làm việc không tồn tại hoặc đã hết hạn!"));

        if (pendingSession.getExpiresAt().isBefore(LocalDateTime.now())) {
            oauth2PendingSessionRepository.delete(pendingSession);
            throw new UnauthorizedException("Phiên làm việc đã hết hạn!");
        }

        Otp otp = otpRepository.findByEmailAndOtpCode(pendingSession.getEmail(), otpCode)
                .orElseThrow(() -> new ResourceNotFoundException("Mã OTP không chính xác!"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpRepository.delete(otp);
            throw new UnauthorizedException("Mã OTP đã hết hạn!");
        }

        // OTP Valid - Find or Create User
        User user = userRepository.findByEmail(pendingSession.getEmail()).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(pendingSession.getEmail());
            newUser.setUsername(pendingSession.getEmail()); // Using email as username
            newUser.setFullName(pendingSession.getFullName());
            newUser.setGoogleId(pendingSession.getGoogleId());
            newUser.setIsActive(true);
            return userRepository.save(newUser);
        });

        // Ensure googleId is set if not already
        if (user.getGoogleId() == null) {
            user.setGoogleId(pendingSession.getGoogleId());
            userRepository.save(user);
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Cleanup
        otpRepository.delete(otp);
        oauth2PendingSessionRepository.delete(pendingSession);

        // Generate JWT (Best practice: Centralized JWT generation)
        return generateAuthResponse(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        String jwt = jwtUtils.generateTokenFromUsername(user.getUsername());
        String refreshToken = UUID.randomUUID().toString();

        UserSession session = new UserSession();
        session.setUser(user);
        session.setSessionToken(refreshToken);
        session.setExpiresAt(LocalDateTime.now().plusDays(7));
        userSessionRepository.save(session);

        return new AuthResponse(jwt, refreshToken, user.getUsername(), user.getEmail());
    }

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

        String jwt = jwtUtils.generateTokenFromUsername(user.getUsername());
        String refreshToken = UUID.randomUUID().toString();

        UserSession session = new UserSession();
        session.setUser(user);
        session.setSessionToken(refreshToken);
        session.setExpiresAt(LocalDateTime.now().plusDays(7));
        userSessionRepository.save(session);

        return new AuthResponse(jwt, refreshToken, user.getUsername(), user.getEmail());
    }
}
