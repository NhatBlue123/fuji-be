package com.example.fuji.service.impl;

import com.example.fuji.dto.UpdateProfileRequest;
import com.example.fuji.dto.UserProfileResponse;
import com.example.fuji.dto.request.ChangePasswordRequest;
import com.example.fuji.entity.User;
import com.example.fuji.repository.UserRepository;
import com.example.fuji.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // ================== GET MY PROFILE ==================
    @Override
    public UserProfileResponse getMyProfileById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));

        return UserProfileResponse.from(user);
    }

    // ================== UPDATE MY PROFILE ==================
    @Override
    @Transactional // Đảm bảo việc lưu vào DB an toàn
    public UserProfileResponse updateMyProfileById(Long userId, UpdateProfileRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Sửa lại tên các hàm set cho đúng chuẩn CamelCase
        user.setFullName(request.getFullName());
        user.setBio(request.getBio());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setJlptLevel(request.getJlptLevel());

        userRepository.save(user);

        return UserProfileResponse.from(user);
    }

    @Autowired
private PasswordEncoder passwordEncoder;

@Override
public void changePassword(Long userId, ChangePasswordRequest request) {

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    // 1️⃣ Kiểm tra mật khẩu hiện tại
    if (!passwordEncoder.matches(
            request.getCurrentPassword(),
            user.getPasswordHash())) {

        throw new RuntimeException("Current password is incorrect");
    }

    // 2️⃣ Encode mật khẩu mới
    String newPasswordHash =
            passwordEncoder.encode(request.getNewPassword());

    // 3️⃣ Lưu vào DB
    user.setPasswordHash(newPasswordHash);

    userRepository.save(user);
}
}

