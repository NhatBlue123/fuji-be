package com.example.fuji.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Data;
import com.example.fuji.enums.LessonType;
import com.example.fuji.enums.VideoType;
import com.example.fuji.enums.TaskType;

@Entity
@Table(name = "lessons", indexes = {
    @Index(name = "idx_course_id", columnList = "course_id"),
    @Index(name = "idx_lesson_order", columnList = "lesson_order"),
    @Index(name = "idx_lessons_course_order", columnList = "course_id, lesson_order")
})
@Data
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "course_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_lessons_course",
            foreignKeyDefinition = "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE"
        )
    )
    private Course course;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "lesson_order", nullable = false)
    private Integer lessonOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_type", nullable = false, length = 20)
    private LessonType lessonType;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "video_type", length = 20)
    private VideoType videoType = VideoType.youtube;

    @Column
    private Integer duration = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", length = 20)
    private TaskType taskType;

    @Column(name = "task_data", columnDefinition = "JSON")
    private String taskData;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "completion_count")
    private Integer completionCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
