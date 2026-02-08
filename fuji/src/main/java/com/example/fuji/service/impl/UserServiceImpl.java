package com.example.fuji.service.impl;

import com.example.fuji.dto.UpdateProfileRequest;
import com.example.fuji.dto.UserProfileResponse;
import com.example.fuji.entity.User;
import com.example.fuji.repository.UserRepository;
import com.example.fuji.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // ================== GET MY PROFILE ==================
    @Override
    public UserProfileResponse getMyProfile(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return toProfileResponse(user);
    }

    // ================== UPDATE MY PROFILE ==================
    @Override
    public UserProfileResponse updateMyProfile(String username, UpdateProfileRequest request) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(request.getFullName());
        userRepository.save(user);

        return toProfileResponse(user);
    }

    // ================== MAPPER ENTITY → DTO ==================
    private UserProfileResponse toProfileResponse(User user) {

        UserProfileResponse res = new UserProfileResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setFullName(user.getFullName());
        res.setCreatedAt(user.getCreatedAt());
        res.setActive(Boolean.TRUE.equals(user.getIsActive()));

        return res;
    }
}
