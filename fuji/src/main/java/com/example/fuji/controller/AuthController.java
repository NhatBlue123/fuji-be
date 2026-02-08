package com.example.fuji.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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
        setAuthCookies(response, authData);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", authData));
    }

    @PostMapping("/oauth2/verify-otp")
    @Operation(summary = "Xác thực OTP sau Google Login")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOAuth2(
            @Valid @RequestBody com.example.fuji.dto.request.OAuth2VerifyOtpRequest request,
            HttpServletResponse response) {
        AuthResponse authData = authService.verifyOAuth2Otp(request.getSessionId(), request.getOtpCode());
        setAuthCookies(response, authData);
        return ResponseEntity.ok(ApiResponse.success("Xác thực Google thành công", authData));
    }

    private void setAuthCookies(HttpServletResponse response, AuthResponse authData) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", authData.getAccessToken())
                .httpOnly(true)
                .secure(false) // Set true in production
                .path("/")
                .maxAge(60 * 60)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", authData.getRefreshToken())
                .httpOnly(true)
                .secure(false) // Set true in production
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie deleteAccess = ResponseCookie.from("accessToken", "").maxAge(0).path("/").build();
        ResponseCookie deleteRefresh = ResponseCookie.from("refreshToken", "").maxAge(0).path("/").build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefresh.toString());

        return ResponseEntity.ok("Đăng xuất thành công");
    }
}
