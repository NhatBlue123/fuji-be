package com.example.fuji.dto.request;

import com.example.fuji.enums.LessonType;
import com.example.fuji.enums.TaskType;
import com.example.fuji.enums.VideoType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonRequestDTO {

    @NotNull(message = "Course ID không được để trống")
    private Long courseId;

    @NotBlank(message = "Tiêu đề bài học không được để trống")
    @Size(max = 200, message = "Tiêu đề không được vượt quá 200 ký tự")
    private String title;

    @NotNull(message = "Loại bài học không được để trống")
    private LessonType lessonType;

    // For video lessons
    private String videoUrl;
    private VideoType videoType;
    private Integer duration;

    // For task lessons
    private TaskType taskType;
    private String taskData; // JSON string

    private String content;

    private Integer lessonOrder;
}
