package com.example.fuji.service.impl;

import com.example.fuji.dto.UpdateProfileRequest;
import com.example.fuji.dto.UserProfileResponse;
import com.example.fuji.dto.request.ChangePasswordRequest;
import com.example.fuji.entity.User;
import com.example.fuji.repository.UserRepository;
import com.example.fuji.service.FileStorageService;
import com.example.fuji.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.fuji.enums.Gender;
import com.example.fuji.enums.JlptLevel;
import com.example.fuji.exception.BadRequestException;
import com.example.fuji.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserProfileResponse getMyProfileById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));

        return UserProfileResponse.from(user);
    }

    private final FileStorageService fileStorageService;

    @Transactional
    @Override
    public UserProfileResponse updateMyProfileById(
            Long userId,
            UpdateProfileRequest request,
            MultipartFile avatar) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // ===== Update text fields only if not null/empty =====

        if (request.getFullName() != null && !request.getFullName().isEmpty()) {
            user.setFullName(request.getFullName().trim());
        }

        if (request.getBio() != null && !request.getBio().isEmpty()) {
            user.setBio(request.getBio().trim());
        }

        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            user.setPhone(request.getPhone().trim());
        }

        if (request.getGender() != null && !request.getGender().isEmpty()) {
            user.setGender(parseGender(request.getGender()));
        }

        if (request.getJlptLevel() != null && !request.getJlptLevel().isEmpty()) {
            user.setJlptLevel(parseJlptLevel(request.getJlptLevel()));
        }

        // ===== Avatar =====

        if (avatar != null && !avatar.isEmpty()) {

            validateAvatar(avatar);

            String avatarUrl = fileStorageService.save(avatar);

            user.setAvatarUrl(avatarUrl);
        }

        userRepository.save(user);

        return UserProfileResponse.from(user);
    }

    private Gender parseGender(String value) {
        try {
            return Gender.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException(
                    "Invalid gender. Allowed: MALE, FEMALE, OTHER");
        }
    }

    private JlptLevel parseJlptLevel(String value) {
        try {
            return JlptLevel.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException(
                    "Invalid JLPT level. Allowed: N1–N5");
        }
    }

    private void validateAvatar(MultipartFile file) {

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("Avatar must be <= 5MB");
        }

        String type = file.getContentType();

        if (type == null ||
                (!type.equals("image/jpeg")
                        && !type.equals("image/png")
                        && !type.equals("image/webp"))) {

            throw new BadRequestException(
                    "Only JPG, PNG, WEBP images are allowed");
        }
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPasswordHash())) {

            throw new RuntimeException("Current password is incorrect");
        }

        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(newPasswordHash);

        userRepository.save(user);
    }
}
