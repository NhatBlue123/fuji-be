package com.example.fuji.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

// Import các Enum phân loại chuẩn
import com.example.fuji.entity.enums.SectionType;
import com.example.fuji.entity.enums.Difficulty;
import com.example.fuji.entity.enums.QuestionType;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity đại diện cho câu hỏi trong đề thi JLPT
 * Đã kết hợp cấu trúc Parent-Child (Mondai) và các Enum phân loại.
 */
@Entity
@Table(name = "jlpt_questions", indexes = {
        @Index(name = "idx_test_id", columnList = "test_id"),
        @Index(name = "idx_question_order", columnList = "question_order")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JlptQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ràng buộc khóa ngoại chặt chẽ (từ code của nhóm)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false, foreignKey = @ForeignKey(name = "fk_questions_test", foreignKeyDefinition = "FOREIGN KEY (test_id) REFERENCES jlpt_tests(id) ON DELETE CASCADE"))
    private JlptTest test;

    // ========================================================================
    // PHẦN MONDAI & PARENT-CHILD (Cấu trúc JLPT chuẩn của bạn)
    // ========================================================================

    @Column(name = "mondai_number", nullable = false)
    private Integer mondaiNumber;

    @Column(name = "mondai_title")
    private String mondaiTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private JlptQuestion parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JlptQuestion> children = new ArrayList<>();

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    // ========================================================================
    // PHẦN PHÂN LOẠI & NỘI DUNG (Kết hợp)
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SectionType section; // Sử dụng Enum SectionType

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 20)
    private QuestionType questionType; // Thêm Enum QuestionType của nhóm

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Difficulty difficulty = Difficulty.MEDIUM; // Thêm mức độ khó

    /**
     * Dùng contentText thay vì questionText để hỗ trợ chứa nguyên đoạn văn dài
     */
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String contentText;

    /**
     * Duplicate field because database has both question_text and content_text
     */
    @Column(name = "content_text", nullable = false, columnDefinition = "TEXT")
    private String contentTextDuplicate;

    // ========================================================================
    // PHẦN MEDIA (Giữ nguyên liên kết MediaFile của bạn vì nó tối ưu hơn)
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_media_id")
    private MediaFile imageMedia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_media_id")
    private MediaFile audioMedia;

    // ========================================================================
    // PHẦN ĐÁP ÁN & CHẤM ĐIỂM
    // ========================================================================

    @Column(columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private String options;

    /**
     * Giữ correctOption (Integer) của bạn thay vì correctAnswer (String)
     * để chấm điểm chính xác và an toàn hơn.
     */
    @Column(name = "correct_answer")
    private Integer correctOption;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal points = BigDecimal.ONE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ========================================================================
    // HELPER METHODS (Giữ nguyên của bạn)
    // ========================================================================

    public boolean isParent() {
        return this.parent == null;
    }

    public boolean isChild() {
        return this.parent != null;
    }

    public void addChild(JlptQuestion child) {
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(JlptQuestion child) {
        children.remove(child);
        child.setParent(null);
    }
}