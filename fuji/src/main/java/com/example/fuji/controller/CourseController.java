package com.example.fuji.controller;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.fuji.dto.request.CourseRequestDTO;
import com.example.fuji.dto.request.CourseUpdateDTO;
import com.example.fuji.dto.request.RatingRequestDTO;
import com.example.fuji.dto.response.ApiResponse;
import com.example.fuji.dto.response.CourseResponseDTO;
import com.example.fuji.dto.response.RatingResponseDTO;
import com.example.fuji.exception.BadRequestException;
import com.example.fuji.service.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "API quản lý khóa học")
@SecurityRequirement(name = "bearerAuth") // để swagger nhận biết token
public class CourseController {

    private final CourseService courseService;
    private final Validator validator;

    @GetMapping
    @Operation(summary = "Lấy tất cả khóa học với phân trang")
    public ResponseEntity<ApiResponse<Page<CourseResponseDTO>>> getAllCourses(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Page<CourseResponseDTO> courses = courseService.getAllCourses(page, size, sortBy, sortDir);

        return ResponseEntity.ok(
                ApiResponse.<Page<CourseResponseDTO>>builder()
                        .success(true)
                        .message("Lấy danh sách khóa học thành công")
                        .data(courses)
                        .build());
    }

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo khóa học mới (Admin only, có thể kèm ảnh thumbnail)")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> createCourse(
            @RequestPart("course") String courseJson,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {

        try {
            // Parse JSON string to DTO (fix Swagger multipart issue)
            ObjectMapper objectMapper = new ObjectMapper();
            CourseRequestDTO courseDTO = objectMapper.readValue(courseJson, CourseRequestDTO.class);

            // Validate manually since @Valid doesn't work with String
            Set<ConstraintViolation<CourseRequestDTO>> violations = validator.validate(courseDTO);
            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("Validation failed");
                throw new BadRequestException(errorMessage);
            }

            CourseResponseDTO createdCourse = courseService.createCourse(courseDTO, thumbnail);

            return ResponseEntity
                    .status(201)
                    .body(ApiResponse.<CourseResponseDTO>builder()
                            .success(true)
                            .message("Tạo khóa học thành công")
                            .data(createdCourse)
                            .build());
        } catch (IOException e) {
            throw new BadRequestException("JSON không hợp lệ: " + e.getMessage());
        }
    }

    @GetMapping("/published")
    @Operation(summary = "Lấy các khóa học đã publish")
    public ResponseEntity<ApiResponse<Page<CourseResponseDTO>>> getPublishedCourses(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Page<CourseResponseDTO> courses = courseService.getPublishedCourses(page, size, sortBy, sortDir);

        return ResponseEntity.ok(
                ApiResponse.<Page<CourseResponseDTO>>builder()
                        .success(true)
                        .message("Lấy danh sách khóa học đã publish thành công")
                        .data(courses)
                        .build());
    }

    @GetMapping("/instructor")
    @Operation(summary = "Lấy khóa học theo giảng viên")
    public ResponseEntity<ApiResponse<Page<CourseResponseDTO>>> getCoursesByInstructor(
            @RequestParam Long instructorId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

        Page<CourseResponseDTO> courses = courseService.getCoursesByInstructor(instructorId, page, size);

        return ResponseEntity.ok(
                ApiResponse.<Page<CourseResponseDTO>>builder()
                        .success(true)
                        .message("Lấy danh sách khóa học của giảng viên thành công")
                        .data(courses)
                        .build());
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm khóa học")
    public ResponseEntity<ApiResponse<Page<CourseResponseDTO>>> searchCourses(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

        Page<CourseResponseDTO> courses = courseService.searchCourses(keyword, page, size);

        return ResponseEntity.ok(
                ApiResponse.<Page<CourseResponseDTO>>builder()
                        .success(true)
                        .message("Tìm kiếm khóa học thành công")
                        .data(courses)
                        .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết khóa học theo ID")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> getCourseById(@PathVariable Long id) {
        CourseResponseDTO course = courseService.getCourseById(id);
        return ResponseEntity.ok(
                ApiResponse.<CourseResponseDTO>builder()
                        .success(true)
                        .message("Lấy thông tin khóa học thành công")
                        .data(course)
                        .build());
    }

    @PatchMapping(value = "/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật thông tin khóa học (Admin only, Partial Update)")
    public ResponseEntity<ApiResponse<CourseResponseDTO>> updateCourse(
            @PathVariable Long id,
            @RequestPart("course") String courseJson,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            CourseUpdateDTO updateDTO = objectMapper.readValue(courseJson, CourseUpdateDTO.class);

            // Validate fields if present
            Set<ConstraintViolation<CourseUpdateDTO>> violations = validator.validate(updateDTO);
            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("Validation failed");
                throw new BadRequestException(errorMessage);
            }

            CourseResponseDTO updatedCourse = courseService.updateCourse(id, updateDTO, thumbnail);

            return ResponseEntity.ok(
                    ApiResponse.<CourseResponseDTO>builder()
                            .success(true)
                            .message("Cập nhật khóa học thành công")
                            .data(updatedCourse)
                            .build());
        } catch (IOException e) {
            throw new BadRequestException("JSON không hợp lệ: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa khóa học và tất cả lessons (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Xóa khóa học và tất cả bài học thành công")
                        .build());
    }

    // ==================== RATING API ====================

    @PostMapping("/{id}/rate")
    @Operation(summary = "Đánh giá khóa học (1-5 sao)")
    public ResponseEntity<ApiResponse<RatingResponseDTO>> rateCourse(
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody RatingRequestDTO ratingDTO) {

        RatingResponseDTO rating = courseService.rateCourse(id, ratingDTO);

        return ResponseEntity.ok(
                ApiResponse.<RatingResponseDTO>builder()
                        .success(true)
                        .message("Đánh giá khóa học thành công")
                        .data(rating)
                        .build());
    }

    @GetMapping("/{id}/ratings")
    @Operation(summary = "Lấy danh sách đánh giá của khóa học")
    public ResponseEntity<ApiResponse<List<RatingResponseDTO>>> getCourseRatings(
            @PathVariable Long id) {

        List<RatingResponseDTO> ratings = courseService.getCourseRatings(id);

        return ResponseEntity.ok(
                ApiResponse.<List<RatingResponseDTO>>builder()
                        .success(true)
                        .message("Lấy danh sách đánh giá thành công")
                        .data(ratings)
                        .build());
    }
}
