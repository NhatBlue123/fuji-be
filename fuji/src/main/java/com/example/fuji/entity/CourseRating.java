package com.example.fuji.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "course_ratings", uniqueConstraints = {
    @UniqueConstraint(name = "uk_rating_user_course", columnNames = {"user_id", "course_id"})
})
@Data
public class CourseRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_ratings_user",
            foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
        )
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "course_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_ratings_course",
            foreignKeyDefinition = "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE"
        )
    )
    private Course course;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String review;

    @Column(name = "helpful_count")
    private Integer helpfulCount = 0;

    @Column(name = "report_count")
    private Integer reportCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
