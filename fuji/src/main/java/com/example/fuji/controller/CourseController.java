package com.example.fuji.controller;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.fuji.dto.request.CourseDTO;
import com.example.fuji.dto.response.ApiResponse;
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
    public ResponseEntity<ApiResponse<Page<CourseDTO>>> getAllCourses(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Page<CourseDTO> courses = courseService.getAllCourses(page, size, sortBy, sortDir);

        return ResponseEntity.ok(
                ApiResponse.<Page<CourseDTO>>builder()
                        .success(true)
                        .message("Lấy danh sách khóa học thành công")
                        .data(courses)
                        .build());
    }

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Tạo khóa học mới (có thể kèm ảnh thumbnail)")
    public ResponseEntity<ApiResponse<CourseDTO>> createCourse(
            @RequestPart("course") String courseJson,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) throws Exception {

        // Parse JSON string to DTO (fix Swagger multipart issue)
        ObjectMapper objectMapper = new ObjectMapper();
        CourseDTO courseDTO = objectMapper.readValue(courseJson, CourseDTO.class);

        // Validate manually since @Valid doesn't work with String
        Set<ConstraintViolation<CourseDTO>> violations = validator.validate(courseDTO);
        if (!violations.isEmpty()) {
            throw new MethodArgumentNotValidException(null,
                    new BeanPropertyBindingResult(courseDTO, "courseDTO"));
        }

        CourseDTO createdCourse = courseService.createCourse(courseDTO, thumbnail);

        return ResponseEntity
                .status(201)
                .body(ApiResponse.<CourseDTO>builder()
                        .success(true)
                        .message("Tạo khóa học thành công")
                        .data(createdCourse)
                        .build());
    }

    @GetMapping("/published")
    @Operation(summary = "Lấy các khóa học đã publish")
    public ResponseEntity<ApiResponse<Page<CourseDTO>>> getPublishedCourses(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Page<CourseDTO> courses = courseService.getPublishedCourses(page, size, sortBy, sortDir);

        return ResponseEntity.ok(
                ApiResponse.<Page<CourseDTO>>builder()
                        .success(true)
                        .message("Lấy danh sách khóa học đã publish thành công")
                        .data(courses)
                        .build());
    }

    @GetMapping("/instructor")
    @Operation(summary = "Lấy khóa học theo giảng viên")
    public ResponseEntity<ApiResponse<Page<CourseDTO>>> getCoursesByInstructor(
            @RequestParam Long instructorId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

        Page<CourseDTO> courses = courseService.getCoursesByInstructor(instructorId, page, size);

        return ResponseEntity.ok(
                ApiResponse.<Page<CourseDTO>>builder()
                        .success(true)
                        .message("Lấy danh sách khóa học của giảng viên thành công")
                        .data(courses)
                        .build());
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm khóa học")
    public ResponseEntity<ApiResponse<Page<CourseDTO>>> searchCourses(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

        Page<CourseDTO> courses = courseService.searchCourses(keyword, page, size);

        return ResponseEntity.ok(
                ApiResponse.<Page<CourseDTO>>builder()
                        .success(true)
                        .message("Tìm kiếm khóa học thành công")
                        .data(courses)
                        .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết khóa học theo ID")
    public ResponseEntity<ApiResponse<CourseDTO>> getCourseById(@PathVariable Long id) {
        CourseDTO course = courseService.getCourseById(id);
        return ResponseEntity.ok(
                ApiResponse.<CourseDTO>builder()
                        .success(true)
                        .message("Lấy thông tin khóa học thành công")
                        .data(course)
                        .build());
    }
}
