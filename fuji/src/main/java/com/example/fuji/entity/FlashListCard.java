package com.example.fuji.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "flash_list_cards", indexes = {
        @Index(name = "idx_flc_list_id", columnList = "list_id"),
        @Index(name = "idx_flc_card_id", columnList = "card_id")
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
    @JoinColumn(name = "list_id", nullable = false, foreignKey = @ForeignKey(name = "fk_flc_list", foreignKeyDefinition = "FOREIGN KEY (list_id) REFERENCES flash_lists(id) ON DELETE CASCADE"))
    private FlashList flashList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false, foreignKey = @ForeignKey(name = "fk_flc_card", foreignKeyDefinition = "FOREIGN KEY (card_id) REFERENCES flash_cards(id) ON DELETE CASCADE"))
    private FlashCard flashCard;

    @Column(name = "card_order", nullable = false)
    private Integer cardOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
