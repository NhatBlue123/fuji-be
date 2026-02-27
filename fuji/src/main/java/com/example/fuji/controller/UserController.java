package com.example.fuji.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API quản lý người dùng")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

        private final UserService userService;

        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Lấy tất cả người dùng với phân trang")
        public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(
                        @RequestParam(defaultValue = "0") @Min(0) int page,
                        @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir) {

                Page<UserDTO> users = userService.getAllUsers(page, size, sortBy, sortDir);

                return ResponseEntity.ok(
                                ApiResponse.<Page<UserDTO>>builder()
                                                .success(true)
                                                .message("Lấy danh sách người dùng thành công")
                                                .data(users)
                                                .build());
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
        @Operation(summary = "Lấy hồ sơ người dùng hiện tại")
        public ResponseEntity<UserProfileResponse> getMyProfile(
                        Authentication authentication) {

                UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                return ResponseEntity.ok(
                                userService.getMyProfileById(principal.getId()));
        }

        @PutMapping("/me")
        @Operation(summary = "Cập nhật hồ sơ người dùng hiện tại")
        public ResponseEntity<UserProfileResponse> updateMyProfile(
                        Authentication authentication,
                        @Valid @RequestBody UpdateProfileRequest request) {

                UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                return ResponseEntity.ok(
                                userService.updateMyProfileById(principal.getId(), request));
        }

        @PutMapping("/me/change-password")
        @Operation(summary = "Đổi mật khẩu")
        public ResponseEntity<?> changePassword(
                        Authentication authentication,
                        @Valid @RequestBody ChangePasswordRequest request) {

                if (authentication == null || !authentication.isAuthenticated()) {
                        return ResponseEntity.status(401).build();
                }

                UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                userService.changePassword(principal.getId(), request);

                return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        }

        @GetMapping("/{id}")
        @Operation(summary = "Lấy chi tiết người dùng theo ID")
        public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
                UserDTO user = userService.getUserById(id);
                return ResponseEntity.ok(
                                ApiResponse.<UserDTO>builder()
                                                .success(true)
                                                .message("Lấy thông tin người dùng thành công")
                                                .data(user)
                                                .build());
        }

        @GetMapping("/email")
        @Operation(summary = "Lấy thông tin người dùng theo email")
        public ResponseEntity<ApiResponse<UserDTO>> getUserByEmail(@RequestParam String email) {
                UserDTO user = userService.getUserByEmail(email);
                return ResponseEntity.ok(
                                ApiResponse.<UserDTO>builder()
                                                .success(true)
                                                .message("Lấy thông tin người dùng thành công")
                                                .data(user)
                                                .build());
        }

        @GetMapping("/username")
        @Operation(summary = "Lấy thông tin người dùng theo username")
        public ResponseEntity<ApiResponse<UserDTO>> getUserByUsername(@RequestParam String username) {
                UserDTO user = userService.getUserByUsername(username);
                return ResponseEntity.ok(
                                ApiResponse.<UserDTO>builder()
                                                .success(true)
                                                .message("Lấy thông tin người dùng thành công")
                                                .data(user)
                                                .build());
        }

        @PostMapping
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Tạo người dùng mới")
        public ResponseEntity<ApiResponse<UserDTO>> createUser(@RequestBody UserDTO userDTO) {
                UserDTO createdUser = userService.createUser(userDTO);
                return ResponseEntity.status(201).body(
                                ApiResponse.<UserDTO>builder()
                                                .success(true)
                                                .message("Tạo người dùng thành công")
                                                .data(createdUser)
                                                .build());
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Cập nhật thông tin người dùng")
        public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
                UserDTO updatedUser = userService.updateUser(id, userDTO);
                return ResponseEntity.ok(
                                ApiResponse.<UserDTO>builder()
                                                .success(true)
                                                .message("Cập nhật thông tin người dùng thành công")
                                                .data(updatedUser)
                                                .build());
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Xóa người dùng")
        public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
                userService.deleteUser(id);
                return ResponseEntity.ok(
                                ApiResponse.<Void>builder()
                                                .success(true)
                                                .message("Xóa người dùng thành công")
                                                .build());
        }
}
