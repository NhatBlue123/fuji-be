package com.example.fuji.dto.request;

import com.example.fuji.enums.LessonType;
import com.example.fuji.enums.TaskType;
import com.example.fuji.enums.VideoType;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonUpdateDTO {

    @Size(max = 200, message = "Tiêu đề không được vượt quá 200 ký tự")
    private String title;

    private LessonType lessonType;

    private String videoUrl;
    private VideoType videoType;
    private Integer duration;

    private TaskType taskType;
    private String taskData;

    private String content;

    private Integer lessonOrder;
}
