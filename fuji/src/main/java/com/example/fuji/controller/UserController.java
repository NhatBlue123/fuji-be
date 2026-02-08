package com.example.fuji.controller;

import com.example.fuji.dto.UpdateProfileRequest;
import com.example.fuji.dto.UserProfileResponse;
import com.example.fuji.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    @GetMapping
    public ResponseEntity<UserProfileResponse> getMyProfile(Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        String username = authentication.getName(); // lấy từ JWT
        System.out.println(username);
        UserProfileResponse response = userService.getMyProfile(username);

        return ResponseEntity.ok(response);
        //return userService.getMyProfile(authentication);
    }
    @PutMapping
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        if (request == null) {
            return ResponseEntity.status(400).build();
        }

        String username = authentication.getName();
        UserProfileResponse response = userService.updateMyProfile(username, request);

        return ResponseEntity.ok(response);
    }
}
