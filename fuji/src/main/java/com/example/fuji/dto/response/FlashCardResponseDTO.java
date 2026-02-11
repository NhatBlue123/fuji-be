package com.example.fuji.dto.response;

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
public class FlashCardResponseDTO {
    private Long id;
    private String name;
    private String description;
    private JlptLevel level;
    private String thumbnailUrl;
    private Boolean isPublic;
    private UserSummaryDTO user;
    private List<CardResponseDTO> cards;
    private Integer cardCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Study tracking fields
    private Long studyCount;
    private UserStudyProgressDTO userProgress;
}
