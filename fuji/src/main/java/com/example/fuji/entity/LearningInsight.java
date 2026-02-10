package com.example.fuji.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Data;
import com.example.fuji.enums.InsightTone;
import com.example.fuji.enums.BestStudyTime;
import com.example.fuji.enums.OverallLevel;

@Entity
@Table(name = "learning_insights", uniqueConstraints = {
    @UniqueConstraint(name = "uk_learning_insights_user_date", columnNames = {"user_id", "analysis_date"})
})
@Data
public class LearningInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_learning_insights_user",
            foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
        )
    )
    private User user;

    @Column(name = "analysis_date", nullable = false)
    private LocalDateTime analysisDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_level", length = 20)
    private OverallLevel overallLevel;

    @Column(name = "weekly_progress", precision = 5, scale = 2)
    private BigDecimal weeklyProgress = BigDecimal.ZERO;

    @Column(name = "consistency_score")
    private Integer consistencyScore = 0;

    @Column(name = "retention_rate", precision = 5, scale = 2)
    private BigDecimal retentionRate = BigDecimal.ZERO;

    @Column(name = "listening_level")
    private Integer listeningLevel = 0;

    @Column(name = "speaking_level")
    private Integer speakingLevel = 0;

    @Column(name = "reading_level")
    private Integer readingLevel = 0;

    @Column(name = "writing_level")
    private Integer writingLevel = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "best_study_time", length = 20)
    private BestStudyTime bestStudyTime;

    @Column(name = "avg_session_length")
    private Integer avgSessionLength = 0;

    @Column(name = "study_frequency", precision = 3, scale = 1)
    private BigDecimal studyFrequency = BigDecimal.ZERO;

    @Column(name = "ai_message", columnDefinition = "TEXT")
    private String aiMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_tone", length = 20)
    private InsightTone aiTone = InsightTone.encouraging;

    @Column(name = "ai_generated_at")
    private LocalDateTime aiGeneratedAt;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Column(name = "confidence_score")
    private Integer confidenceScore = 0;

    @Column(name = "data_points_analyzed")
    private Integer dataPointsAnalyzed = 0;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
