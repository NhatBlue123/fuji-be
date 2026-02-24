package com.example.fuji.dto.request;

import java.math.BigDecimal;

import com.example.fuji.entity.enums.SectionType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để tạo câu hỏi JLPT mới
 * 
 * THAY ĐỔI CHÍNH:
 * 1. Thêm mondaiNumber, mondaiTitle
 * 2. Thêm parentId (để tạo câu hỏi con)
 * 3. Đổi imageUrl, audioUrl → imageMediaId, audioMediaId
 * 4. Bỏ questionText/passageText → chỉ có contentText
 * 5. Đổi correctAnswer → correctOption
 * 6. Bỏ difficulty, questionType (không cần cho JLPT thật)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionDTO {

    // ========================================================================
    // MONDAI (Tổ chức theo bài lớn)
    // ========================================================================

    @NotNull(message = "Số thứ tự Mondai không được để trống")
    @Min(value = 1, message = "Mondai phải >= 1")
    private Integer mondaiNumber;

    /**
     * Tiêu đề Mondai (optional)
     * VD: "Problem 1: Kanji Reading"
     */
    private String mondaiTitle;

    // ========================================================================
    // PARENT-CHILD
    // ========================================================================

    /**
     * ID của câu hỏi cha (NULL nếu đây là parent)
     * 
     * - NULL: Tạo đoạn văn (parent)
     * - NOT NULL: Tạo câu hỏi con thuộc đoạn văn có ID này
     */
    private Long parentId;

    @NotNull(message = "Thứ tự câu hỏi không được để trống")
    @Min(value = 1, message = "Thứ tự câu hỏi phải >= 1")
    private Integer questionOrder;

    // ========================================================================
    // NỘI DUNG
    // ========================================================================

    @NotNull(message = "Section không được để trống")
    private SectionType section;

    /**
     * Nội dung (context-aware):
     * - Nếu parentId = NULL: Đây là đoạn văn/hội thoại
     * - Nếu parentId != NULL: Đây là câu hỏi
     */
    @NotBlank(message = "Nội dung không được để trống")
    private String contentText;

    // ========================================================================
    // MEDIA (Liên kết với MediaFile)
    // ========================================================================

    /**
     * ID của file ảnh trong bảng media_files (optional)
     */
    private Long imageMediaId;

    /**
     * ID của file audio trong bảng media_files (optional)
     */
    private Long audioMediaId;

    // ========================================================================
    // ĐÁP ÁN (Chỉ dùng cho câu hỏi, không dùng cho parent)
    // ========================================================================

    /**
     * Lựa chọn (JSON string)
     * VD: "[\"1. に\", \"2. へ\", \"3. で\", \"4. を\"]"
     * 
     * NULL nếu đây là parent (đoạn văn)
     */
    private String options;

    /**
     * Đáp án đúng (1, 2, 3, 4)
     * 
     * NULL nếu đây là parent (đoạn văn)
     */
    @Min(value = 1, message = "Đáp án đúng phải >= 1")
    private Integer correctOption;

    /**
     * Giải thích đáp án (optional)
     */
    private String explanation;

    /**
     * Điểm số (mặc định 1.0 nếu không truyền)
     */
    @Min(value = 0, message = "Điểm số phải >= 0")
    private BigDecimal points;
}
