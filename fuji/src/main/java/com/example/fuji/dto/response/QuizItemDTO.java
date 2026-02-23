package com.example.fuji.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizItemDTO {
    private Long cardId;
    private String question;
    private String correctAnswer;
    private List<String> options;
}
