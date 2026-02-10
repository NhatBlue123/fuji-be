package com.example.fuji.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "jlpt_test_attempts")
@Data
public class JlptTestAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_attempts_user",
            foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
        )
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "test_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_attempts_test",
            foreignKeyDefinition = "FOREIGN KEY (test_id) REFERENCES jlpt_tests(id) ON DELETE CASCADE"
        )
    )
    private JlptTest test;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "max_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxScore;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;

    @Column(name = "is_passed", nullable = false)
    private Boolean isPassed;

    @Column(name = "vocabulary_score", precision = 5, scale = 2)
    private BigDecimal vocabularyScore = BigDecimal.ZERO;

    @Column(name = "grammar_score", precision = 5, scale = 2)
    private BigDecimal grammarScore = BigDecimal.ZERO;

    @Column(name = "reading_score", precision = 5, scale = 2)
    private BigDecimal readingScore = BigDecimal.ZERO;

    @Column(name = "listening_score", precision = 5, scale = 2)
    private BigDecimal listeningScore = BigDecimal.ZERO;

    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "time_spent", nullable = false)
    private Integer timeSpent;

    @Column(nullable = false, columnDefinition = "JSON")
    private String answers;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @CreationTimestamp
    @Column(name = "completed_at", nullable = false, updatable = false)
    private LocalDateTime completedAt;
}
