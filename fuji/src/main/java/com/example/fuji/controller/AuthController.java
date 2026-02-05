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
import com.example.fuji.dto.response.LoginResponse;
import com.example.fuji.dto.response.RefreshResponse;
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
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody AuthDTO authRequest,
            jakarta.servlet.http.HttpServletResponse response) {

        AuthResponse authResponse = authService.login(authRequest);

        // Set refreshToken vào HttpOnly cookie (bảo mật cao)
        jakarta.servlet.http.Cookie refreshCookie = new jakarta.servlet.http.Cookie("refreshToken", authResponse.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // Set true khi deploy production với HTTPS
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
        response.addCookie(refreshCookie);

        // ✅ Access token trả về trong JSON body (client tự quản lý)
        LoginResponse loginResponse = new LoginResponse(
            authResponse.getAccessToken(),
            authResponse.getUsername()
        );

        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", loginResponse));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới access token")
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(
            @CookieValue(name = "refreshToken", required = true) String refreshToken,
            jakarta.servlet.http.HttpServletResponse response) {

        // ✅ Refresh token CHỈ đọc từ HttpOnly cookie (không cho phép từ body)

        AuthResponse authResponse = authService.refreshAccessToken(refreshToken);

        // ✅ Rotate refresh token (set cookie mới)
        jakarta.servlet.http.Cookie newRefreshCookie = new jakarta.servlet.http.Cookie("refreshToken", authResponse.getRefreshToken());
        newRefreshCookie.setHttpOnly(true);
        newRefreshCookie.setSecure(false);  // TODO: Set true khi deploy production
        newRefreshCookie.setPath("/");
        newRefreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(newRefreshCookie);

        // ✅ Chỉ trả về access token mới trong JSON
        RefreshResponse refreshResponse = new RefreshResponse(authResponse.getAccessToken());

        return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công", refreshResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất")
    public ResponseEntity<ApiResponse<String>> logout(jakarta.servlet.http.HttpServletResponse response) {
        Long userId = authUtils.getCurrentUserId();
        authService.logout(userId);

        // Xóa refresh token cookie

        jakarta.servlet.http.Cookie refreshCookie = new jakarta.servlet.http.Cookie("refreshToken", null);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        refreshCookie.setHttpOnly(true);
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công"));
    }
}
