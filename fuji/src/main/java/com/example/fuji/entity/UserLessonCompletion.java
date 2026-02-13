package com.example.fuji.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_lesson_completions", indexes = {
    @Index(name = "idx_completion_user_lesson", columnList = "user_id, lesson_id"),
    @Index(name = "idx_completion_user_course", columnList = "user_id, course_id"),
    @Index(name = "idx_completion_completed_at", columnList = "user_id, completed_at")
})
@Data
public class UserLessonCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_completion_user",
            foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
        )
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "lesson_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_completion_lesson",
            foreignKeyDefinition = "FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE"
        )
    )
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "course_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_completion_course",
            foreignKeyDefinition = "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE"
        )
    )
    private Course course;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "max_score", precision = 5, scale = 2)
    private BigDecimal maxScore;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber = 1;

    @Column(name = "time_spent")
    private Integer timeSpent = 0;

    @CreationTimestamp
    @Column(name = "completed_at", nullable = false, updatable = false)
    private LocalDateTime completedAt;
}
