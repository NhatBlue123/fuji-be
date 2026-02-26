package com.example.fuji.controller;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.fuji.dto.request.LessonRequestDTO;
import com.example.fuji.dto.request.LessonUpdateDTO;
import com.example.fuji.dto.response.ApiResponse;
import com.example.fuji.dto.response.LessonResponseDTO;
import com.example.fuji.exception.BadRequestException;
import com.example.fuji.service.LessonService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@Tag(name = "Lessons", description = "API quản lý bài học trong khóa học")
@SecurityRequirement(name = "bearerAuth")
public class LessonController {

    private final LessonService lessonService;
    private final Validator validator;

    // ==================== API 7: createLesson ====================

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo bài học mới trong khóa học (Admin only)")
    public ResponseEntity<ApiResponse<LessonResponseDTO>> createLesson(
            @RequestPart("lesson") String lessonJson,
            @RequestPart(value = "video", required = false) MultipartFile videoFile) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            LessonRequestDTO lessonDTO = objectMapper.readValue(lessonJson, LessonRequestDTO.class);

            Set<ConstraintViolation<LessonRequestDTO>> violations = validator.validate(lessonDTO);
            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("Validation failed");
                throw new BadRequestException(errorMessage);
            }

            LessonResponseDTO createdLesson = lessonService.createLesson(lessonDTO, videoFile);

            return ResponseEntity
                    .status(201)
                    .body(ApiResponse.<LessonResponseDTO>builder()
                            .success(true)
                            .message("Tạo bài học thành công")
                            .data(createdLesson)
                            .build());
        } catch (IOException e) {
            throw new BadRequestException("JSON không hợp lệ: " + e.getMessage());
        }
    }

    // ==================== API 8: getLessonById ====================

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết bài học theo ID")
    public ResponseEntity<ApiResponse<LessonResponseDTO>> getLessonById(@PathVariable Long id) {
        LessonResponseDTO lesson = lessonService.getLessonById(id);
        return ResponseEntity.ok(
                ApiResponse.<LessonResponseDTO>builder()
                        .success(true)
                        .message("Lấy thông tin bài học thành công")
                        .data(lesson)
                        .build());
    }

    // ==================== API 9: updateLesson ====================

    @PatchMapping(value = "/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật bài học (Admin only)")
    public ResponseEntity<ApiResponse<LessonResponseDTO>> updateLesson(
            @PathVariable Long id,
            @RequestPart("lesson") String lessonJson,
            @RequestPart(value = "video", required = false) MultipartFile videoFile) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            LessonUpdateDTO updateDTO = objectMapper.readValue(lessonJson, LessonUpdateDTO.class);

            Set<ConstraintViolation<LessonUpdateDTO>> violations = validator.validate(updateDTO);
            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("Validation failed");
                throw new BadRequestException(errorMessage);
            }

            LessonResponseDTO updatedLesson = lessonService.updateLesson(id, updateDTO, videoFile);

            return ResponseEntity.ok(
                    ApiResponse.<LessonResponseDTO>builder()
                            .success(true)
                            .message("Cập nhật bài học thành công")
                            .data(updatedLesson)
                            .build());
        } catch (IOException e) {
            throw new BadRequestException("JSON không hợp lệ: " + e.getMessage());
        }
    }

    // ==================== API 10: deleteLesson ====================

    @DeleteMapping("/{lessonId}/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa bài học khỏi khóa học (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(
            @PathVariable Long lessonId,
            @PathVariable Long courseId) {

        lessonService.deleteLesson(lessonId, courseId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Xóa bài học thành công")
                        .build());
    }

    // ==================== Get all lessons by course ====================

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Lấy tất cả bài học theo khóa học")
    public ResponseEntity<ApiResponse<List<LessonResponseDTO>>> getLessonsByCourse(
            @PathVariable Long courseId) {

        List<LessonResponseDTO> lessons = lessonService.getLessonsByCourse(courseId);

        return ResponseEntity.ok(
                ApiResponse.<List<LessonResponseDTO>>builder()
                        .success(true)
                        .message("Lấy danh sách bài học thành công")
                        .data(lessons)
                        .build());
    }
}
