package com.example.fuji.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cards", indexes = {
    @Index(name = "idx_card_flashcard_id", columnList = "flashcard_id"),
    @Index(name = "idx_card_order", columnList = "card_order")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "flashcard_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_cards_flashcard",
            foreignKeyDefinition = "FOREIGN KEY (flashcard_id) REFERENCES flash_cards(id) ON DELETE CASCADE"
        )
    )
    private FlashCard flashCard;

    @Column(nullable = false, length = 200)
    private String vocabulary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String meaning;

    @Column(length = 200)
    private String pronunciation;

    @Column(name = "example_sentence", columnDefinition = "TEXT")
    private String exampleSentence;

    @Column(name = "card_order", nullable = false)
    private Integer cardOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
