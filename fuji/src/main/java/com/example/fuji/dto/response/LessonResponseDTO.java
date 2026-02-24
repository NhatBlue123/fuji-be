package com.example.fuji.dto.response;

import java.time.LocalDateTime;

import com.example.fuji.enums.LessonType;
import com.example.fuji.enums.TaskType;
import com.example.fuji.enums.VideoType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonResponseDTO {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String title;
    private Integer lessonOrder;
    private LessonType lessonType;

    // Video fields
    private String videoUrl;
    private VideoType videoType;
    private Integer duration;

    // Task fields
    private TaskType taskType;
    private String taskData;

    private String content;
    private Integer completionCount;
    private Boolean userCompleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
