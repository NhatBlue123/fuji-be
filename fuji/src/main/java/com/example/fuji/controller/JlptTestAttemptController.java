package com.example.fuji.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.fuji.dto.response.ApiResponse;
import com.example.fuji.dto.response.TestAttemptResponseDTO;
import com.example.fuji.service.JlptTestAttemptService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

import com.example.fuji.entity.User;
import com.example.fuji.utils.AuthUtils;

@RestController
@RequestMapping("/api/jlpt-test-attempts")
@SecurityRequirement(name = "bearerAuth")
public class JlptTestAttemptController {

    private final JlptTestAttemptService attemptService;
    private final AuthUtils authUtils;

    public JlptTestAttemptController(JlptTestAttemptService attemptService, AuthUtils authUtils) {
        super();
        this.attemptService = attemptService;
        this.authUtils = authUtils;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết kết quả thi")
    public ResponseEntity<ApiResponse<TestAttemptResponseDTO>> getAttemptById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Chi tiết kết quả thi", attemptService.getAttemptById(id)));
    }

    @GetMapping("/my-attempts")
    @Operation(summary = "Lấy lịch sử thi của user hiện tại")
    public ResponseEntity<ApiResponse<List<TestAttemptResponseDTO>>> getMyAttempts() {
        User currentUser = authUtils.getCurrentUser();
        return ResponseEntity
                .ok(ApiResponse.success("Lịch sử thi", attemptService.getAttemptsByUserId(currentUser.getId())));
    }
}
