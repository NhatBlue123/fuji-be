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
public class RatingResponseDTO {
    private Long id;
    private Long courseId;
    private Integer rating;
    private String review;
    private UserSummaryDTO user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
