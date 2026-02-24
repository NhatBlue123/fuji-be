package com.example.fuji.dto.request;

import java.math.BigDecimal;

import com.example.fuji.entity.enums.SectionType;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để cập nhật câu hỏi JLPT (Partial Update)
 * 
 * Tất cả các field đều optional
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuestionDTO {

    @Min(value = 1, message = "Mondai phải >= 1")
    private Integer mondaiNumber;

    private String mondaiTitle;

    /**
     * Không cho phép thay đổi parentId sau khi tạo
     * (Nếu muốn chuyển câu hỏi sang parent khác, phải xóa và tạo lại)
     */
    // private Long parentId; // KHÔNG CHO SỬA

    @Min(value = 1, message = "Thứ tự câu hỏi phải >= 1")
    private Integer questionOrder;

    private SectionType section;

    private String contentText;

    private Long imageMediaId;

    private Long audioMediaId;

    private String options;

    @Min(value = 1, message = "Đáp án đúng phải >= 1")
    private Integer correctOption;

    private String explanation;

    @Min(value = 0, message = "Điểm số phải >= 0")
    private BigDecimal points;
}
