package com.example.fuji.middleware;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Logging Filter - Log tất cả requests
 * Giống middleware logging trong Express
 */
@Slf4j
@Component
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();

        // Log request
        log.info("→ {} {} from {}",
            req.getMethod(),
            req.getRequestURI(),
            req.getRemoteAddr()
        );

        // Continue request
        chain.doFilter(request, response);

        // Log response
        long duration = System.currentTimeMillis() - startTime;
        log.info("← {} {} - Status: {} - Duration: {}ms",
            req.getMethod(),
            req.getRequestURI(),
            res.getStatus(),
            duration
        );
    }
}
