package com.example.fuji.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.fuji.enums.JlptLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashListResponseDTO {
    private Long id;
    private String title;
    private String description;
    private JlptLevel level;
    private String thumbnailUrl;
    private Boolean isPublic;
    private UserSummaryDTO user;
    private List<FlashCardSummaryDTO> flashcards;
    private Integer cardCount;
    private BigDecimal averageRating;
    private Integer ratingCount;
    private Integer studyCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
