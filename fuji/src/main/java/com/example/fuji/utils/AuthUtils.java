package com.example.fuji.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.fuji.entity.User;
import com.example.fuji.exception.ResourceNotFoundException;
import com.example.fuji.repository.UserRepository;
import com.example.fuji.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthUtils {

    private final UserRepository userRepository;

    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getUsername();
        }
        return principal.toString();
    }

    // Lấy userId trực tiếp từ SecurityContext (không query DB)
    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResourceNotFoundException("Bạn cần đăng nhập để thực hiện thao tác này");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getId();
        }
        throw new ResourceNotFoundException("Không thể lấy thông tin user");
    }

    // Chỉ query DB khi thật sự cần User entity
    public User getCurrentUser() {
        Long userId = getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại với id: " + userId));
    }

    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
            && auth.isAuthenticated()
            && auth.getPrincipal() instanceof UserPrincipal;
    }

    public boolean isCurrentUser(Long userId) {
        if (!isAuthenticated()) {
            return false;
        }
        return getCurrentUserId().equals(userId);
    }
}
