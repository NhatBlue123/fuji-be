package com.example.fuji.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.fuji.entity.enums.JLPTLevel;
import com.example.fuji.entity.enums.TestType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "jlpt_tests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JlptTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private JLPTLevel level;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", nullable = false, length = 20)
    private TestType testType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer duration; // in minutes

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "max_score")
    @Builder.Default
    private Integer maxScore = 180;

    @Column(name = "passing_score", nullable = false, columnDefinition = "INT DEFAULT 90")
    @Builder.Default
    private Integer passScore = 90;

    @Column(name = "language_knowledge_pass_score")
    @Builder.Default
    private Integer languageKnowledgePassScore = 19;

    @Column(name = "reading_pass_score")
    @Builder.Default
    private Integer readingPassScore = 19;

    @Column(name = "listening_pass_score")
    @Builder.Default
    private Integer listeningPassScore = 19;

    @Column(name = "attempt_count")
    @Builder.Default
    private Integer attemptCount = 0;

    @Column(name = "average_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal averageScore = BigDecimal.ZERO;

    @Column(name = "is_published")
    @Builder.Default
    private Boolean isPublished = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
