package com.example.fuji.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseResultRequest {
    private String exerciseType; // "multiple_choice" or "fill_blank"
    private Integer correctCount;
    private Integer totalCount;
}
