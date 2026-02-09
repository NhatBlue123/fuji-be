package com.example.fuji.controller;

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
import com.example.fuji.dto.request.VerifyOtpDTO;
import com.example.fuji.dto.response.ApiResponse;
import com.example.fuji.dto.response.AuthResponse;
import com.example.fuji.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API xác thực người dùng")
public class AuthController {

    private final AuthService authService;
    private final com.example.fuji.utils.AuthUtils authUtils;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản mới")
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
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthDTO authRequest,
            HttpServletResponse response) {
        AuthResponse authData = authService.login(authRequest);
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", authData.getAccessToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", authData.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 ngày
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", authData));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = "refreshToken", required = false) String cookieToken,
            @RequestBody(required = false) java.util.Map<String, String> body) {

        // Ưu tiên cookie, fallback sang body
        String refreshToken = cookieToken != null ? cookieToken : (body != null ? body.get("refreshToken") : null);

        if (refreshToken == null) {
            throw new com.example.fuji.exception.UnauthorizedException("Refresh token không được cung cấp");
        }

        AuthResponse response = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất")
    public ResponseEntity<ApiResponse<String>> logout() {
        Long userId = authUtils.getCurrentUserId();
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công"));
    }
}
