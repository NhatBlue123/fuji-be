package com.example.fuji.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponseDTO {
    private Long id;
    private String title;
    private String description;

    // Nested objects - không có instructorId
    private UserSummaryDTO instructor;
    private UserSummaryDTO author;

    private String thumbnailUrl;
    private BigDecimal price;
    private Integer studentCount;
    private Integer lessonCount;
    private Integer totalDuration;
    private BigDecimal averageRating;
    private Integer ratingCount;
    private Boolean isPublished;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
