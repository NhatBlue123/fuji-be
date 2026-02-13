package com.example.fuji.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "daily_learning_stats", uniqueConstraints = {
    @UniqueConstraint(name = "uk_daily_stats_user_date", columnNames = {"user_id", "date"})
})
@Data
public class DailyLearningStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_daily_stats_user",
            foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
        )
    )
    private User user;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(name = "total_study_time")
    private Integer totalStudyTime = 0;

    @Column(name = "lessons_completed")
    private Integer lessonsCompleted = 0;

    @Column(name = "cards_reviewed")
    private Integer cardsReviewed = 0;

    @Column(name = "cards_learned")
    private Integer cardsLearned = 0;

    @Column(name = "correct_rate", precision = 5, scale = 2)
    private java.math.BigDecimal correctRate = java.math.BigDecimal.ZERO;

    @Column(name = "streak_days")
    private Integer streakDays = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
