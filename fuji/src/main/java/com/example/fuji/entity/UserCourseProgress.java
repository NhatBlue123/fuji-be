package com.example.fuji.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Data;
import com.example.fuji.enums.ProgressStatus;

@Entity
@Table(name = "user_course_progress", uniqueConstraints = {
    @UniqueConstraint(name = "uk_progress_user_course", columnNames = {"user_id", "course_id"})
}, indexes = {
    @Index(name = "idx_progress_user_status", columnList = "user_id, status"),
    @Index(name = "idx_progress_last_accessed", columnList = "user_id, last_accessed_at")
})
@Data
public class UserCourseProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_progress_user",
            foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
        )
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "course_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_progress_course",
            foreignKeyDefinition = "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE"
        )
    )
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "current_lesson_id",
        foreignKey = @ForeignKey(
            name = "fk_progress_lesson",
            foreignKeyDefinition = "FOREIGN KEY (current_lesson_id) REFERENCES lessons(id) ON DELETE SET NULL"
        )
    )
    private Lesson currentLesson;

    @Column(name = "video_timestamp")
    private Integer videoTimestamp = 0;

    @CreationTimestamp
    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private LocalDateTime enrolledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgressStatus status = ProgressStatus.not_started;

    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "total_time_spent")
    private Integer totalTimeSpent = 0;

    @Column(name = "lessons_completed")
    private Integer lessonsCompleted = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
