package com.example.fuji.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fuji.dto.response.QuizItemDTO;
import com.example.fuji.dto.response.StudyQuizDTO;
import com.example.fuji.entity.Card;
import com.example.fuji.entity.FlashCard;
import com.example.fuji.entity.FlashList;
import com.example.fuji.entity.FlashListCard;
import com.example.fuji.exception.ResourceNotFoundException;
import com.example.fuji.repository.CardRepository;
import com.example.fuji.repository.FlashCardRepository;
import com.example.fuji.repository.FlashListCardRepository;
import com.example.fuji.repository.FlashListRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service tạo quiz học tập từ FlashCard hoặc FlashList.
 * Sinh câu hỏi vocab-to-meaning và meaning-to-vocab với multiple choice.
 */
@Service
@RequiredArgsConstructor
public class StudyService {

    private final FlashCardRepository flashCardRepository;
    private final FlashListRepository flashListRepository;
    private final FlashListCardRepository flashListCardRepository;
    private final CardRepository cardRepository;

    @Transactional(readOnly = true)
    public StudyQuizDTO getStudyDataFromFlashCard(Long cardId) {
        FlashCard flashCard = flashCardRepository.findByIdAndDeletedAtIsNull(cardId)
            .orElseThrow(() -> new ResourceNotFoundException("FlashCard không tồn tại với id: " + cardId));

        List<Card> cards = cardRepository.findByFlashCardIdOrderByCardOrderAsc(flashCard.getId());
        return generateQuiz(cards);
    }

    @Transactional
    public StudyQuizDTO getStudyDataFromFlashList(Long listId) {
        FlashList flashList = flashListRepository.findByIdAndDeletedAtIsNull(listId)
            .orElseThrow(() -> new ResourceNotFoundException("FlashList không tồn tại với id: " + listId));

        flashList.incrementStudyCount();
        flashListRepository.save(flashList);

        List<FlashListCard> flashListCards = flashListCardRepository.findByFlashListIdOrderByCardOrderAsc(listId);

        List<Card> allCards = new ArrayList<>();
        for (FlashListCard flc : flashListCards) {
            List<Card> cards = cardRepository.findByFlashCardIdOrderByCardOrderAsc(flc.getFlashCard().getId());
            allCards.addAll(cards);
        }

        return generateQuiz(allCards);
    }

    private StudyQuizDTO generateQuiz(List<Card> cards) {
        if (cards.isEmpty()) {
            return StudyQuizDTO.builder()
                .vocabToMeaning(Collections.emptyList())
                .meaningToVocab(Collections.emptyList())
                .totalCards(0)
                .build();
        }

        List<String> allVocabs = cards.stream().map(Card::getVocabulary).collect(Collectors.toList());
        List<String> allMeanings = cards.stream().map(Card::getMeaning).collect(Collectors.toList());

        List<Card> shuffledCards = new ArrayList<>(cards);
        Collections.shuffle(shuffledCards);

        List<QuizItemDTO> vocabToMeaning = shuffledCards.stream()
            .map(card -> createVocabToMeaningQuiz(card, allMeanings))
            .collect(Collectors.toList());

        Collections.shuffle(shuffledCards);

        List<QuizItemDTO> meaningToVocab = shuffledCards.stream()
            .map(card -> createMeaningToVocabQuiz(card, allVocabs))
            .collect(Collectors.toList());

        return StudyQuizDTO.builder()
            .vocabToMeaning(vocabToMeaning)
            .meaningToVocab(meaningToVocab)
            .totalCards(cards.size())
            .build();
    }

    private QuizItemDTO createVocabToMeaningQuiz(Card card, List<String> allMeanings) {
        List<String> options = generateOptions(card.getMeaning(), allMeanings);
        return QuizItemDTO.builder()
            .cardId(card.getId())
            .question(card.getVocabulary())
            .correctAnswer(card.getMeaning())
            .options(options)
            .build();
    }

    private QuizItemDTO createMeaningToVocabQuiz(Card card, List<String> allVocabs) {
        List<String> options = generateOptions(card.getVocabulary(), allVocabs);
        return QuizItemDTO.builder()
            .cardId(card.getId())
            .question(card.getMeaning())
            .correctAnswer(card.getVocabulary())
            .options(options)
            .build();
    }

    private List<String> generateOptions(String correctAnswer, List<String> allOptions) {
        List<String> options = new ArrayList<>();
        options.add(correctAnswer);

        List<String> wrongOptions = allOptions.stream()
            .filter(opt -> !opt.equals(correctAnswer))
            .collect(Collectors.toList());
        Collections.shuffle(wrongOptions);

        int maxWrongOptions = Math.min(3, wrongOptions.size());
        for (int i = 0; i < maxWrongOptions; i++) {
            options.add(wrongOptions.get(i));
        }

        Collections.shuffle(options);
        return options;
    }
}
