package com.example.fuji.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.fuji.dto.request.CourseDTO;
import com.example.fuji.dto.response.ApiResponse;
import com.example.fuji.service.CourseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "API quản lý khóa học")
public class CourseController {

    private final CourseService courseService;

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
                .build()
        );
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
                .build()
        );
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
                .build()
        );
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
                .build()
        );
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
                .build()
        );
    }
}
