package com.example.fuji.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity lưu kết quả bài thi JLPT của user
 * Đã merge: Giữ logic chấm điểm chuẩn JLPT (HEAD) + Thêm Khóa ngoại FK Cascade
 * (Team)
 */
@Entity
@Table(name = "jlpt_test_attempts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JlptTestAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User đã làm bài thi này (Dùng cấu trúc FK an toàn của nhóm)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_attempts_user", foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"))
    private User user;

    /**
     * Đề thi đã làm (Dùng cấu trúc FK an toàn của nhóm)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false, foreignKey = @ForeignKey(name = "fk_attempts_test", foreignKeyDefinition = "FOREIGN KEY (test_id) REFERENCES jlpt_tests(id) ON DELETE CASCADE"))
    private JlptTest test;

    // ========================================================================
    // PHẦN KẾT QUẢ TỔNG QUAN (Logic của bạn)
    // ========================================================================

    /**
     * Tổng điểm đạt được (VD: 125/180)
     */
    @Column(name = "total_score", precision = 5, scale = 2)
    private BigDecimal totalScore;

    /**
     * Duplicate field for 'score' column required by actual database
     */
    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    /**
     * Điểm tối đa của đề thi (Snapshot tại thời điểm thi)
     */
    @Column(name = "max_score", nullable = false)
    private Integer maxScore;

    /**
     * Tỉ lệ phần trăm điểm đạt được (VD: 75.5%)
     */
    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    /**
     * Trạng thái đỗ/trượt
     */
    @Column(name = "is_passed")
    private Boolean isPassed;

    // ========================================================================
    // PHẦN ĐIỂM THÀNH PHẦN (Logic chuẩn JLPT của bạn)
    // ========================================================================

    /**
     * Điểm phần Kiến thức ngôn ngữ (Vocabulary + Grammar)
     * Max: 60/180
     */
    @Column(name = "language_knowledge_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal languageKnowledgeScore = BigDecimal.ZERO;

    /**
     * Điểm phần Đọc hiểu
     * Max: 60/180
     */
    @Column(name = "reading_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal readingScore = BigDecimal.ZERO;

    /**
     * Điểm phần Nghe hiểu
     * Max: 60/180
     */
    @Column(name = "listening_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal listeningScore = BigDecimal.ZERO;

    // ========================================================================
    // PHẦN THỐNG KÊ
    // ========================================================================

    @Column(name = "correct_answers")
    private Integer correctAnswers;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    /**
     * Thời gian làm bài (giây)
     */
    @Column(name = "time_spent")
    private Integer timeSpent;

    // ========================================================================
    // CHI TIẾT BÀI LÀM
    // ========================================================================

    /**
     * Lưu chi tiết từng câu trả lời (JSON)
     */

    @Column(name = "user_answers", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private String userAnswers;

    /**
     * Duplicate field for 'answers' column required by database
     */
    @Column(name = "answers", columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private String answers;

    // ========================================================================
    // TIMESTAMPS
    // ========================================================================

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ========================================================================
    // ANTI-CHEAT (EXAM LIFECYCLE TRACKING)
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private com.example.fuji.entity.enums.AttemptStatus status = com.example.fuji.entity.enums.AttemptStatus.IN_PROGRESS;

    /**
     * Thời lượng bài thi được snapshot tại thời điểm bắt đầu (phút)
     */
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    /**
     * Thời gian hết hạn = startedAt + durationMinutes
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}