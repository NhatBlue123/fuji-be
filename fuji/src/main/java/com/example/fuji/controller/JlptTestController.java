package com.example.fuji.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.fuji.dto.request.CreateJlptTestDTO;
import com.example.fuji.dto.request.CreateQuestionDTO;
import com.example.fuji.dto.request.SubmitTestAttemptDTO;
import com.example.fuji.dto.request.UpdateJlptTestDTO;
import com.example.fuji.dto.request.UpdateQuestionDTO;
import com.example.fuji.dto.response.ApiResponse;
import com.example.fuji.dto.response.JlptTestResponseDTO;
import com.example.fuji.dto.response.QuestionResponseDTO;
import com.example.fuji.dto.response.TestAttemptResponseDTO;
import com.example.fuji.entity.User;
import com.example.fuji.entity.enums.JLPTLevel;
import com.example.fuji.service.JlptTestAttemptService;
import com.example.fuji.service.JlptTestService;
import com.example.fuji.utils.AuthUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller quản lý đề thi JLPT
 * (Redesigned with Parent-Child Structure)
 */
@RestController
@RequestMapping("/api/jlpt-tests")
@RequiredArgsConstructor
@Tag(name = "JLPT Test Management", description = "API quản lý đề thi JLPT (New Structure)")
@SecurityRequirement(name = "bearerAuth")
public class JlptTestController {

    private final JlptTestService testService;
    private final JlptTestAttemptService attemptService;
    private final AuthUtils authUtils;

    // ========================================================================
    // QUẢN LÝ ĐỀ THI (ADMIN)
    // ========================================================================

    @PostMapping
    @Operation(summary = "Tạo đề thi mới")
    public ResponseEntity<ApiResponse<JlptTestResponseDTO>> createTest(@Valid @RequestBody CreateJlptTestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Tạo đề thi thành công", testService.createTest(dto)));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách đề thi (Admin view)")
    public ResponseEntity<ApiResponse<Page<JlptTestResponseDTO>>> getAllTests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity
                .ok(ApiResponse.success("Danh sách đề thi", testService.getAllTests(page, size, sortBy, sortDir)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết đề thi (kèm câu hỏi)")
    public ResponseEntity<ApiResponse<JlptTestResponseDTO>> getTestById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Chi tiết đề thi", testService.getTestById(id)));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Cập nhật đề thi")
    public ResponseEntity<ApiResponse<JlptTestResponseDTO>> updateTest(
            @PathVariable Long id,
            @RequestBody UpdateJlptTestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", testService.updateTest(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa đề thi")
    public ResponseEntity<ApiResponse<Void>> deleteTest(@PathVariable Long id) {
        testService.deleteTest(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa đề thi thành công"));
    }

    // ========================================================================
    // QUẢN LÝ CÂU HỎI (ADMIN)
    // ========================================================================

    @PostMapping("/{testId}/questions")
    @Operation(summary = "Thêm câu hỏi vào đề thi (Parent hoặc Single Question)")
    public ResponseEntity<ApiResponse<QuestionResponseDTO>> addQuestion(
            @PathVariable Long testId,
            @Valid @RequestBody CreateQuestionDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Thêm câu hỏi thành công", testService.addQuestion(testId, dto)));
    }

    @PostMapping("/{testId}/questions/parent/{parentId}/child")
    @Operation(summary = "Thêm câu hỏi con vào một đoạn văn (Parent)")
    public ResponseEntity<ApiResponse<QuestionResponseDTO>> addChildQuestion(
            @PathVariable Long testId,
            @PathVariable Long parentId,
            @Valid @RequestBody CreateQuestionDTO dto) {
        // Force set parentId
        dto.setParentId(parentId);
        return ResponseEntity
                .ok(ApiResponse.success("Thêm câu hỏi con thành công", testService.addQuestion(testId, dto)));
    }

    @PatchMapping("/questions/{id}")
    @Operation(summary = "Cập nhật câu hỏi")
    public ResponseEntity<ApiResponse<QuestionResponseDTO>> updateQuestion(
            @PathVariable Long id,
            @RequestBody UpdateQuestionDTO dto) {
        return ResponseEntity
                .ok(ApiResponse.success("Cập nhật câu hỏi thành công", testService.updateQuestion(id, dto)));
    }

    @DeleteMapping("/questions/{id}")
    @Operation(summary = "Xóa câu hỏi")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable Long id) {
        testService.deleteQuestion(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa câu hỏi thành công"));
    }

    // ========================================================================
    // DÀNH CHO STUDENT
    // ========================================================================

    @GetMapping("/published")
    @Operation(summary = "Lấy danh sách đề thi (Student view - Published only)")
    public ResponseEntity<ApiResponse<Page<JlptTestResponseDTO>>> getPublishedTests(
            @RequestParam(required = false) JLPTLevel level,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity
                .ok(ApiResponse.success("Danh sách đề thi",
                        testService.getPublishedTestsByLevel(level, search, page, size)));
    }

    @PostMapping("/submit")
    @Operation(summary = "Nộp bài thi")
    public ResponseEntity<ApiResponse<TestAttemptResponseDTO>> submitTest(
            @Valid @RequestBody SubmitTestAttemptDTO dto) {
        User currentUser = authUtils.getCurrentUser();
        return ResponseEntity
                .ok(ApiResponse.success("Nộp bài thành công", attemptService.submitAttempt(currentUser.getId(), dto)));
    }
}
