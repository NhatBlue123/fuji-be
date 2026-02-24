package com.example.fuji.service;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fuji.dto.UpdateProfileRequest;
import com.example.fuji.dto.UserProfileResponse;
import com.example.fuji.dto.request.ChangePasswordRequest;
import com.example.fuji.dto.response.UserDTO;
import com.example.fuji.entity.User;
import com.example.fuji.enums.Gender;
import com.example.fuji.enums.JlptLevel;
import com.example.fuji.enums.Role;
import com.example.fuji.exception.ResourceNotFoundException;
import com.example.fuji.repository.UserRepository;
import com.example.fuji.utils.AuthUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AuthUtils authUtils;
    private final RefreshTokenService refreshTokenService;

    public UserProfileResponse getMyProfileById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));

        return UserProfileResponse.from(user);
    }

    // ================== UPDATE MY PROFILE ==================
    @Transactional // Đảm bảo việc lưu vào DB an toàn
    public UserProfileResponse updateMyProfileById(Long userId, UpdateProfileRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Sửa lại tên các hàm set cho đúng chuẩn CamelCase
        user.setFullName(request.getFullName());
        user.setBio(request.getBio());
        user.setPhone(request.getPhone());
        user.setGender(parseGender(request.getGender()));
        user.setJlptLevel(parseJlptLevel(request.getJlptLevel()));

        userRepository.save(user);

        return UserProfileResponse.from(user);
    }

    private Gender parseGender(String gender) {
        if (gender == null)
            return null;

        try {
            return Gender.valueOf(gender.trim().toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid gender value");
        }
    }

    private JlptLevel parseJlptLevel(String level) {
        if (level == null)
            return null;

        try {
            return JlptLevel.valueOf(level.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid JLPT level");
        }
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());

        // 3️⃣ Lưu vào DB
        user.setPasswordHash(newPasswordHash);

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users = userRepository.findAll(pageable);

        return users.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public UserDTO getMe() {
        User currentUser = authUtils.getCurrentUser();
        return convertToDTO(currentUser);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại với id: " + id));
        return convertToDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại với email: " + email));
        return convertToDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại với username: " + username));
        return convertToDTO(user);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getInstructors() {
        List<User> instructors = userRepository.findByRole(Role.INSTRUCTOR);
        return instructors.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        // TODO: Implement user creation logic
        throw new UnsupportedOperationException("Chức năng tạo user chưa được implement");
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại với id: " + id));

        // Track if role is changing
        boolean roleChanging = false;
        if (userDTO.getRole() != null && !userDTO.getRole().equals(user.getRole().name())) {
            roleChanging = true;
            user.setRole(Role.valueOf(userDTO.getRole()));
        }

        // Update other fields if provided
        if (userDTO.getFullName() != null && !userDTO.getFullName().isBlank()) {
            user.setFullName(userDTO.getFullName());
        }
        if (userDTO.getAvatarUrl() != null && !userDTO.getAvatarUrl().isBlank()) {
            user.setAvatarUrl(userDTO.getAvatarUrl());
        }

        // Save updated user
        User updatedUser = userRepository.save(user);

        // If role changed, revoke all refresh tokens so user must re-authenticate with new permissions
        if (roleChanging) {
            refreshTokenService.revokeAllUserTokens(user.getId());
        }

        return convertToDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Người dùng không tồn tại với id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name()) // Convert enum to String
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
