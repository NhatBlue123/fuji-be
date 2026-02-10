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
public class StudyQuizDTO {
    private List<QuizItemDTO> vocabToMeaning;
    private List<QuizItemDTO> meaningToVocab;
    private int totalCards;
}
