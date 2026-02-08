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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

        // Process OAuth2 Login (Create pending session and send OTP)
        String sessionId = authService.processOAuth2Login(email, googleId, name);

        // Redirect to Frontend Login Page with Session Context
        String targetUrl = frontendUrl + "/login?session=" + sessionId + "&email=" + email;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
