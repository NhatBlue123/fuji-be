package com.example.fuji.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "flash_list_cards", uniqueConstraints = {
    @UniqueConstraint(name = "uk_flashlist_flashcard", columnNames = {"list_id", "flashcard_id"})
}, indexes = {
    @Index(name = "idx_flashlist_card_list_id", columnList = "list_id"),
    @Index(name = "idx_flashlist_card_flashcard_id", columnList = "flashcard_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashListCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "list_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_flash_list_cards_list",
            foreignKeyDefinition = "FOREIGN KEY (list_id) REFERENCES flash_lists(id) ON DELETE CASCADE"
        )
    )
    private FlashList flashList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "flashcard_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_flash_list_cards_flashcard",
            foreignKeyDefinition = "FOREIGN KEY (flashcard_id) REFERENCES flash_cards(id) ON DELETE CASCADE"
        )
    )
    private FlashCard flashCard;

    @Column(name = "card_order")
    @Builder.Default
    private Integer cardOrder = 0;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;
}
