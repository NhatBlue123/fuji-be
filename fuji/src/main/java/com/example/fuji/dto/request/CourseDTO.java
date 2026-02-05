package com.example.fuji.dto.request;
//định nghia kiểu object CourseDTO dùng để truyền dữ liệu khóa học trong các request... khi fe call api thì phải truyền đúng kiểu này
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
public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private String instructorName;
    private Long instructorId;
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
