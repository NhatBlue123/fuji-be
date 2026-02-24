package com.example.fuji.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.fuji.dto.request.MediaDTO;
import com.example.fuji.entity.enums.SectionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO response cho thông tin câu hỏi JLPT
 * 
 * Hỗ trợ cấu trúc parent-child:
 * - Nếu là parent: children != null (danh sách câu hỏi con)
 * - Nếu là child: parentId != null
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponseDTO {

    private Long id;
    private Long testId;

    // Mondai
    private Integer mondaiNumber;
    private String mondaiTitle;

    // Parent-Child
    private Long parentId;
    private Integer questionOrder;

    // Content
    private SectionType section;
    private String contentText;

    // Media (thông tin đầy đủ, không chỉ ID)
    private MediaDTO imageMedia;
    private MediaDTO audioMedia;

    // Đáp án (NULL nếu là parent)
    private String options;
    private Integer correctOption;
    private String explanation;
    private BigDecimal points;

    // Timestamps
    private LocalDateTime createdAt;

    /**
     * Danh sách câu hỏi con (chỉ có khi đây là parent)
     */
    private List<QuestionResponseDTO> children;

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Kiểm tra xem đây có phải parent không
     */
    public boolean isParent() {
        return this.parentId == null;
    }

    /**
     * Kiểm tra xem đây có phải child không
     */
    public boolean isChild() {
        return this.parentId != null;
    }

    /**
     * Ẩn đáp án (dùng khi trả về cho student đang làm bài)
     * 
     * @return QuestionResponseDTO với correctOption và explanation = null
     */
    public QuestionResponseDTO hideAnswer() {
        this.correctOption = null;
        this.explanation = null;

        // Nếu có children, ẩn đáp án của children luôn
        if (this.children != null && !this.children.isEmpty()) {
            this.children.forEach(QuestionResponseDTO::hideAnswer);
        }

        return this;
    }
}
