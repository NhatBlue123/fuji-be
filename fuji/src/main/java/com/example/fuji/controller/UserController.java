package com.example.fuji.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.fuji.dto.UpdateProfileRequest;
import com.example.fuji.dto.UserProfileResponse;
import com.example.fuji.dto.request.ChangePasswordRequest;
import com.example.fuji.dto.response.ApiResponse;
import com.example.fuji.dto.response.UserDTO;
import com.example.fuji.security.UserPrincipal;
import com.example.fuji.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getMyProfile(
            Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(
                userService.getMyProfileById(principal.getId()));
    }

    @GetMapping("/instructors")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách giảng viên (ADMIN only)")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getInstructors() {
        List<UserDTO> instructors = userService.getInstructors();
        return ResponseEntity.ok(
                ApiResponse.<List<UserDTO>>builder()
                        .success(true)
                        .message("Lấy danh sách giảng viên thành công")
                        .data(instructors)
                        .build());
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin người dùng hiện tại")
    public ResponseEntity<ApiResponse<UserDTO>> getMe() {
        UserDTO user = userService.getMe();
        return ResponseEntity.ok(
                ApiResponse.<UserDTO>builder()
                        .success(true)
                        .message("Lấy thông tin người dùng thành công")
                        .data(user)
                        .build());
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(
                userService.updateMyProfileById(principal.getId(), request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        userService.changePassword(principal.getId(), request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật thông tin người dùng (chỉ ADMIN)")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(
                ApiResponse.<UserDTO>builder()
                        .success(true)
                        .message("Cập nhật thông tin người dùng thành công")
                        .data(updatedUser)
                        .build());
    }
}
