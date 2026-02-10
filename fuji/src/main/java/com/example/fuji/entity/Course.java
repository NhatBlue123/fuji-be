package com.example.fuji.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "courses", indexes = {
    @Index(name = "idx_instructor_id", columnList = "instructor_id"),
    @Index(name = "idx_created_by", columnList = "created_by"),
    @Index(name = "idx_is_published", columnList = "is_published"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_instructor_published", columnList = "instructor_id, is_published"),
    @Index(name = "idx_published_rating", columnList = "is_published, average_rating")
})
@Data
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT NOT NULL")
    private String description = "";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "instructor_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_courses_instructor",
            foreignKeyDefinition = "FOREIGN KEY (instructor_id) REFERENCES users(id) ON DELETE RESTRICT"
        )
    )
    private User instructor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "created_by",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_courses_created_by",
            foreignKeyDefinition = "FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT"
        )
    )
    private User createdBy;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "student_count", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer studentCount = 0;

    @Column(name = "lesson_count", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer lessonCount = 0;

    @Column(name = "total_duration", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer totalDuration = 0;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "rating_count", columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer ratingCount = 0;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
