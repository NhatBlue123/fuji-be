package com.example.fuji.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_flashcard_study", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_flashcard_study", columnNames = {"user_id", "flashcard_id"})
}, indexes = {
    @Index(name = "idx_user_flashcard_study_user", columnList = "user_id"),
    @Index(name = "idx_user_flashcard_study_flashcard", columnList = "flashcard_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFlashCardStudy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_user_flashcard_study_user",
            foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
        )
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "flashcard_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_user_flashcard_study_flashcard",
            foreignKeyDefinition = "FOREIGN KEY (flashcard_id) REFERENCES flash_cards(id) ON DELETE CASCADE"
        )
    )
    private FlashCard flashCard;

    @Column(name = "progress_percentage")
    @Builder.Default
    private Integer progressPercentage = 0;

    @Column(name = "remembered_count")
    @Builder.Default
    private Integer rememberedCount = 0;

    @Column(name = "total_cards")
    @Builder.Default
    private Integer totalCards = 0;

    @Column(name = "last_studied_at")
    private LocalDateTime lastStudiedAt;

    @Column(name = "next_review_at")
    private LocalDateTime nextReviewAt;

    @Column(name = "is_completed")
    @Builder.Default
    private Boolean isCompleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
