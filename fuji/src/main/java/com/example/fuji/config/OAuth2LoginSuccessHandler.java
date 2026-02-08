package com.example.fuji.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.example.fuji.service.AuthService;

import com.example.fuji.dto.response.AuthResponse;
import com.example.fuji.dto.response.OAuth2LoginResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;

    @Value("${fuji.app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = token.getPrincipal();

        String email = oauth2User.getAttribute("email");
        String googleId = oauth2User.getAttribute("sub");
        String name = oauth2User.getAttribute("name");

        log.info("OAuth2 Authenticated by Google: email={}", email);

        // Process OAuth2 Login
        OAuth2LoginResult result = authService.processOAuth2Login(email, googleId, name);

        if (result.isNeedsOtp()) {
            // Redirect to Frontend Login Page with Session Context for OTP
            String targetUrl = frontendUrl + "/login?session=" + result.getSessionId() + "&email=" + email;
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } else {
            // Existing user - Skip OTP, set cookies and redirect home
            setAuthCookies(response, result.getAuthResponse());
            getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/");
        }
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
}
