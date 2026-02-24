package com.example.fuji.dto.request;

import com.example.fuji.entity.enums.JLPTLevel;
import com.example.fuji.entity.enums.TestType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để tạo đề thi JLPT mới
 * 
 * THAY ĐỔI SO VỚI VERSION CŨ:
 * - Thêm maxScore (mặc định 180)
 * - Đổi passingScore → passScore (integer thay vì decimal)
 * - Thêm 3 điểm liệt: languageKnowledgePassScore, readingPassScore,
 * listeningPassScore
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateJlptTestDTO {

    @NotBlank(message = "Tiêu đề đề thi không được để trống")
    @Size(max = 200, message = "Tiêu đề không được vượt quá 200 ký tự")
    private String title;

    @NotNull(message = "Level JLPT không được để trống")
    private JLPTLevel level;

    @NotNull(message = "Loại đề thi không được để trống")
    private TestType testType;

    private String description;

    @NotNull(message = "Thời gian làm bài không được để trống")
    @Min(value = 1, message = "Thời gian làm bài phải lớn hơn 0")
    private Integer duration;

    @NotNull(message = "Tổng số câu hỏi không được để trống")
    @Min(value = 1, message = "Tổng số câu hỏi phải lớn hơn 0")
    private Integer totalQuestions;

    // ========================================================================
    // CẤU HÌNH ĐIỂM SỐ
    // ========================================================================

    /**
     * Tổng điểm tối đa (mặc định 180 nếu không truyền)
     */
    @Min(value = 1, message = "Tổng điểm tối đa phải lớn hơn 0")
    private Integer maxScore;

    /**
     * Điểm đỗ tổng
     * VD: N2 = 90, N1 = 100
     */
    @NotNull(message = "Điểm đỗ không được để trống")
    @Min(value = 0, message = "Điểm đỗ phải >= 0")
    private Integer passScore;

    /**
     * Điểm liệt phần Kiến thức ngôn ngữ
     * Mặc định: 19
     */
    @Min(value = 0, message = "Điểm liệt phải >= 0")
    private Integer languageKnowledgePassScore;

    /**
     * Điểm liệt phần Đọc hiểu
     * Mặc định: 19
     */
    @Min(value = 0, message = "Điểm liệt phải >= 0")
    private Integer readingPassScore;

    /**
     * Điểm liệt phần Nghe hiểu
     * Mặc định: 19
     */
    @Min(value = 0, message = "Điểm liệt phải >= 0")
    private Integer listeningPassScore;

    /**
     * Trạng thái publish (mặc định false)
     */
    private Boolean isPublished;
}
