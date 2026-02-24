package com.example.fuji.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.fuji.entity.enums.JLPTLevel;
import com.example.fuji.entity.enums.TestType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO response cho thông tin đề thi JLPT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JlptTestResponseDTO {

    private Long id;
    private String title;
    private JLPTLevel level;
    private TestType testType;
    private String description;
    private Integer duration;
    private Integer totalQuestions;

    // Cấu hình điểm số
    private Integer maxScore;
    private Integer passScore;
    private Integer languageKnowledgePassScore;
    private Integer readingPassScore;
    private Integer listeningPassScore;

    // Thống kê
    private Integer attemptCount;
    private BigDecimal averageScore;
    private Boolean isPublished;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Danh sách câu hỏi (optional - chỉ load khi cần)
     * Sẽ được tổ chức theo cấu trúc parent-child
     */
    private List<QuestionResponseDTO> questions;
}
