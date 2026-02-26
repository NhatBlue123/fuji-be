package com.example.fuji.dto.request;

import com.example.fuji.entity.enums.JLPTLevel;
import com.example.fuji.entity.enums.TestType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để cập nhật đề thi JLPT (Partial Update)
 * 
 * Tất cả các field đều optional
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJlptTestDTO {

    @Size(max = 200, message = "Tiêu đề không được vượt quá 200 ký tự")
    private String title;

    private JLPTLevel level;

    private TestType testType;

    private String description;

    @Min(value = 1, message = "Thời gian làm bài phải lớn hơn 0")
    private Integer duration;

    @Min(value = 1, message = "Tổng số câu hỏi phải lớn hơn 0")
    private Integer totalQuestions;

    @Min(value = 1, message = "Tổng điểm tối đa phải lớn hơn 0")
    private Integer maxScore;

    @Min(value = 0, message = "Điểm đỗ phải >= 0")
    private Integer passScore;

    @Min(value = 0, message = "Điểm liệt phải >= 0")
    private Integer languageKnowledgePassScore;

    @Min(value = 0, message = "Điểm liệt phải >= 0")
    private Integer readingPassScore;

    @Min(value = 0, message = "Điểm liệt phải >= 0")
    private Integer listeningPassScore;

    private Boolean isPublished;
}
