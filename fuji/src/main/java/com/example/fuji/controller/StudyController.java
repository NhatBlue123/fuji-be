package com.example.fuji.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.fuji.dto.response.ApiResponse;
import com.example.fuji.dto.response.StudyQuizDTO;
import com.example.fuji.service.StudyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    @GetMapping("/card/{cardId}")
    public ResponseEntity<ApiResponse<StudyQuizDTO>> getStudyDataFromFlashCard(@PathVariable Long cardId) {
        StudyQuizDTO quiz = studyService.getStudyDataFromFlashCard(cardId);
        return ResponseEntity.ok(ApiResponse.<StudyQuizDTO>builder()
            .success(true)
            .message("Lấy dữ liệu học tập thành công")
            .data(quiz)
            .build());
    }

    @GetMapping("/list/{listId}")
    public ResponseEntity<ApiResponse<StudyQuizDTO>> getStudyDataFromFlashList(@PathVariable Long listId) {
        StudyQuizDTO quiz = studyService.getStudyDataFromFlashList(listId);
        return ResponseEntity.ok(ApiResponse.<StudyQuizDTO>builder()
            .success(true)
            .message("Lấy dữ liệu học tập thành công")
            .data(quiz)
            .build());
    }
}
