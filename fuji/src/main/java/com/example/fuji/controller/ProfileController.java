package com.example.fuji.controller;

import com.example.fuji.dto.UpdateProfileRequest;
import com.example.fuji.dto.UserProfileResponse;
import com.example.fuji.entity.User;
import com.example.fuji.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).body("Chưa đăng nhập");
        }

        Object principal = authentication.getPrincipal();
        return ResponseEntity.ok(principal);
    }
//     public UserProfileResponse getMyProfile(Authentication authentication) {
//     String username = authentication.getName();
//     User user = userRepository.findByUsername(username);
//     return mapper.toResponse(user);
// }

    @PutMapping
    public UserProfileResponse updateMyProfile(
            Authentication authentication,
            @RequestBody UpdateProfileRequest request) {
        User user = (User) authentication.getPrincipal();
        return userService.updateMyProfile(user.getId(), request);
    }
}
