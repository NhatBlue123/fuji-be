package com.example.fuji.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fuji.dto.request.AuthDTO;
import com.example.fuji.dto.request.RegisterDTO;
import com.example.fuji.dto.request.SendOtpRegisterDTO;
import com.example.fuji.dto.request.VerifyOtpDTO;
import com.example.fuji.dto.response.ApiResponse;
import com.example.fuji.dto.response.AuthResponse;
import com.example.fuji.dto.response.LoginResponse;
import com.example.fuji.dto.response.RefreshResponse;
import com.example.fuji.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

//import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API xác thực người dùng")
public class AuthController {

    private final AuthService authService;
    private final com.example.fuji.utils.AuthUtils authUtils;

    @PostMapping("/send-otp-register")
    @Operation(summary = "Validate thông tin + Gửi OTP để đăng ký - KHÔNG tạo tài khoản")
    public ResponseEntity<ApiResponse<String>> sendOtpRegister(@Valid @RequestBody SendOtpRegisterDTO request) {
        String message = authService.sendOtpRegister(request);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản - gửi đầy đủ thông tin + OTP")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterDTO request) {
        String message = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Xác thực OTP")
    public ResponseEntity<ApiResponse<String>> verify(@Valid @RequestBody VerifyOtpDTO request) {
        String result = authService.verifyOtp(request.getEmail(), request.getOtpCode());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody AuthDTO authRequest,
            jakarta.servlet.http.HttpServletResponse response) {

        AuthResponse authResponse = authService.login(authRequest);

        // Set refreshToken vào HttpOnly cookie (bảo mật cao) with SameSite
        addRefreshTokenCookie(response, authResponse.getRefreshToken());

        // Access token trả về trong JSON body (client tự quản lý)
        LoginResponse loginResponse = new LoginResponse(
                authResponse.getAccessToken(),
                authResponse.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", loginResponse));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới access token")
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            jakarta.servlet.http.HttpServletResponse response) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Refresh token không tồn tại"));
        }

        // ✅ Refresh token CHỈ đọc từ HttpOnly cookie (không cho phép từ body)
        AuthResponse authResponse = authService.refreshAccessToken(refreshToken);

        // ✅ Rotate refresh token (set cookie mới) with SameSite
        addRefreshTokenCookie(response, authResponse.getRefreshToken());

        // Chỉ trả về access token mới trong JSON
        RefreshResponse refreshResponse = new RefreshResponse(authResponse.getAccessToken());

        return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công", refreshResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất")
    public ResponseEntity<ApiResponse<String>> logout(jakarta.servlet.http.HttpServletResponse response) {
        Long userId = authUtils.getCurrentUserId();
        authService.logout(userId);

        // Xóa refresh token cookie
        clearRefreshTokenCookie(response);

        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công"));
    }

    // ─── Cookie Helpers ───────────────────────────────────────

    private void addRefreshTokenCookie(jakarta.servlet.http.HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(false) // NOTE: true khi deploy production với HTTPS
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Lax") // Cho phép same-site requests (localhost)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(jakarta.servlet.http.HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
