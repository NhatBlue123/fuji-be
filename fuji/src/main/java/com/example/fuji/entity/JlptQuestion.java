package com.example.fuji.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.Data;
import com.example.fuji.enums.JlptSection;
import com.example.fuji.enums.QuestionType;
import com.example.fuji.enums.Difficulty;

@Entity
@Table(name = "jlpt_questions", indexes = {
    @Index(name = "idx_test_id", columnList = "test_id"),
    @Index(name = "idx_question_order", columnList = "question_order")
})
@Data
public class JlptQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "test_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_questions_test",
            foreignKeyDefinition = "FOREIGN KEY (test_id) REFERENCES jlpt_tests(id) ON DELETE CASCADE"
        )
    )
    private JlptTest test;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JlptSection section;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 20)
    private QuestionType questionType;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    @Column(nullable = false, columnDefinition = "JSON")
    private String options;

    @Column(name = "correct_answer", nullable = false, length = 500)
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Difficulty difficulty = Difficulty.medium;

    @Column(precision = 5, scale = 2)
    private BigDecimal points = BigDecimal.ONE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
