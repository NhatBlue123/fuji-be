package com.example.fuji.config;

import com.example.fuji.entity.User;
import com.example.fuji.repository.UserRepository;
import com.example.fuji.utils.JwtUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String googleId = oauthUser.getAttribute("sub"); // Google user ID

        User user = userRepository.findByEmail(email).orElse(null);

        // 🔥 Nếu user chưa tồn tại → tạo mới
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setFullName(name);
            user.setGoogleId(googleId);
            user.setPasswordHash(UUID.randomUUID().toString()); // dummy password
            user.setCreatedAt(LocalDateTime.now());
        }

        // 🔥 Cập nhật login time
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // 🔥 TẠO JWT TOKEN
        String token = jwtUtils.generateTokenFromUsername(user.getEmail());

        // ================= OPTION 1: Redirect kèm token =================
        String redirectUrl = "http://localhost:3000/oauth2/success?token=" + token;
        response.sendRedirect(redirectUrl);

        // ================= OPTION 2: Set Cookie (production) =================
        /*
        Cookie cookie = new Cookie("fuji_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true nếu HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);
        response.sendRedirect("http://localhost:3000/");
        */
    }
}
