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
public class CardResponseDTO {
    private Long id;
    private String vocabulary;
    private String meaning;
    private String pronunciation;
    private String exampleSentence;
    private String previewUrl;
    private Integer cardOrder;
    private LocalDateTime createdAt;
}
