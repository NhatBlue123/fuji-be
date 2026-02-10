package com.example.fuji.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.Data;
import com.example.fuji.enums.MasteryLevel;

@Entity
@Table(name = "user_card_progress", uniqueConstraints = {
    @UniqueConstraint(name = "uk_card_progress_user_card", columnNames = {"user_id", "card_id"})
}, indexes = {
    @Index(name = "idx_card_progress_next_review", columnList = "user_id, next_review_at")
})
@Data
public class UserCardProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_card_progress_user",
            foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
        )
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "card_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_card_progress_card",
            foreignKeyDefinition = "FOREIGN KEY (card_id) REFERENCES flash_cards(id) ON DELETE CASCADE"
        )
    )
    private FlashCard card;

    @Enumerated(EnumType.STRING)
    @Column(name = "mastery_level", nullable = false, length = 20)
    private MasteryLevel masteryLevel = MasteryLevel.learning;

    @Column(name = "correct_count")
    private Integer correctCount = 0;

    @Column(name = "incorrect_count")
    private Integer incorrectCount = 0;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @Column(name = "next_review_at")
    private LocalDateTime nextReviewAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
