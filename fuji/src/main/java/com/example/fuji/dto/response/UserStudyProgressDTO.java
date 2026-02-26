package com.example.fuji.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStudyProgressDTO {
    private Long id;
    private Integer progressPercentage;
    private Integer rememberedCount;
    private Integer totalCards;
    private LocalDateTime lastStudiedAt;
    private LocalDateTime nextReviewAt;
    private Boolean isCompleted;
}
