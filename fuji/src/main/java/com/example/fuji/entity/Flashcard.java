package com.example.fuji.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "flashcards")
@Data
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String kanji;

    @Column(nullable = false)
    private String hiragana;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String meaning;

    @Column(columnDefinition = "TEXT")
    private String example;

    @Column(name = "lesson")
    private String lesson; // Storing as string for now to match FE, or strictly if Lesson entity exists?

    @Column(name = "type")
    private String type;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
