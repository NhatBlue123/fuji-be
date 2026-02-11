package com.example.fuji.dto.response;

import com.example.fuji.enums.JlptLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashCardSummaryDTO {
    private Long id;
    private String name;
    private JlptLevel level;
    private String thumbnailUrl;
    private Integer cardCount;
}
