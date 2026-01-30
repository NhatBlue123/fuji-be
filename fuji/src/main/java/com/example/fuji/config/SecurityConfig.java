package com.example.fuji.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@Slf4j
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/media/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    log.error("=== 403 ACCESS DENIED ===");
                    log.error("URL: {}", request.getRequestURL());
                    log.error("Method: {}", request.getMethod());
                    log.error("Error: {}", accessDeniedException.getMessage());

                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    String json = String.format(
                        "{\"timestamp\":\"%s\",\"status\":403,\"error\":\"Forbidden\",\"message\":\"Bạn không có quyền truy cập tài nguyên này\"}",
                        java.time.LocalDateTime.now()
                    );
                    response.getWriter().write(json);
                })
                .authenticationEntryPoint((request, response, authException) -> {
                    log.error("=== 401 UNAUTHORIZED ===");
                    log.error("URL: {}", request.getRequestURL());
                    log.error("Method: {}", request.getMethod());
                    log.error("Error: {}", authException.getMessage());

                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    String json = String.format(
                        "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Bạn cần đăng nhập để truy cập tài nguyên này\"}",
                        java.time.LocalDateTime.now()
                    );
                    response.getWriter().write(json);
                })
            );
        return http.build();
    }


}
