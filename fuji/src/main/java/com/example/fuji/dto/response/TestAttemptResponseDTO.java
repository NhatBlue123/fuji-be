package com.example.fuji.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO response cho kết quả bài thi JLPT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestAttemptResponseDTO {

    private Long id;
    private Long userId;
    private Long testId;

    // Kết quả tổng quan
    private BigDecimal totalScore;
    private Boolean isPassed;

    // Điểm thành phần
    private BigDecimal languageKnowledgeScore;
    private BigDecimal readingScore;
    private BigDecimal listeningScore;

    // Thống kê
    private Integer correctAnswers;
    private Integer totalQuestions;
    private Integer timeSpent;

    /**
     * Chi tiết bài làm (JSON string)
     * 
     * Chứa danh sách câu trả lời với:
     * - question_id
     * - selected (câu trả lời của user)
     * - correct (đáp án đúng)
     * - is_correct (true/false)
     */
    private String userAnswers;

    // Timestamps
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    /**
     * Thông tin đề thi (optional - để hiển thị tên đề)
     */
    private JlptTestResponseDTO test;
}
