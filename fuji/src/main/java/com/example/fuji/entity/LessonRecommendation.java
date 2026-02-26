package com.example.fuji.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.Data;
import com.example.fuji.enums.RecommendationPriority;
import com.example.fuji.enums.RecommendationReason;

@Entity
@Table(name = "lesson_recommendations")
@Data
public class LessonRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_recommendations_user",
            foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
        )
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "lesson_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_recommendations_lesson",
            foreignKeyDefinition = "FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE"
        )
    )
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "course_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_recommendations_course",
            foreignKeyDefinition = "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE"
        )
    )
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RecommendationPriority priority = RecommendationPriority.medium;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RecommendationReason reason;

    @Column(precision = 5, scale = 2)
    private BigDecimal score = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
