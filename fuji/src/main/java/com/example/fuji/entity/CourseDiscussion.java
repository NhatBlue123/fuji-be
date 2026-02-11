package com.example.fuji.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "course_discussions", indexes = {
    @Index(name = "idx_discussion_course", columnList = "course_id"),
    @Index(name = "idx_discussion_lesson", columnList = "lesson_id"),
    @Index(name = "idx_discussion_user", columnList = "user_id"),
    @Index(name = "idx_discussion_parent", columnList = "parent_id")
})
@Data
public class CourseDiscussion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_discussions_user",
            foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
        )
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "course_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_discussions_course",
            foreignKeyDefinition = "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE"
        )
    )
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "lesson_id",
        foreignKey = @ForeignKey(
            name = "fk_discussions_lesson",
            foreignKeyDefinition = "FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE"
        )
    )
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "parent_id",
        foreignKey = @ForeignKey(
            name = "fk_discussions_parent",
            foreignKeyDefinition = "FOREIGN KEY (parent_id) REFERENCES course_discussions(id) ON DELETE CASCADE"
        )
    )
    private CourseDiscussion parent;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_pinned")
    private Boolean isPinned = false;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "reply_count")
    private Integer replyCount = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
