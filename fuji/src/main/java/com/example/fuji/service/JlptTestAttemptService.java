package com.example.fuji.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fuji.dto.request.SubmitTestAttemptDTO;
import com.example.fuji.dto.response.JlptTestResponseDTO;
import com.example.fuji.dto.response.TestAttemptResponseDTO;
import com.example.fuji.entity.JlptQuestion;
import com.example.fuji.entity.JlptTest;
import com.example.fuji.entity.JlptTestAttempt;
import com.example.fuji.entity.User;
import com.example.fuji.exception.BadRequestException;
import com.example.fuji.exception.ResourceNotFoundException;
import com.example.fuji.repository.JlptQuestionRepository;
import com.example.fuji.repository.JlptTestAttemptRepository;
import com.example.fuji.repository.JlptTestRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Service xử lý logic nộp bài và tính điểm
 */
@Service
public class JlptTestAttemptService {

    private final JlptTestRepository testRepository;
    private final JlptQuestionRepository questionRepository;
    private final JlptTestAttemptRepository attemptRepository;
    private final ObjectMapper objectMapper;

    public JlptTestAttemptService(
            JlptTestRepository testRepository,
            JlptQuestionRepository questionRepository,
            JlptTestAttemptRepository attemptRepository,
            ObjectMapper objectMapper) {
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public TestAttemptResponseDTO submitAttempt(Long userId, SubmitTestAttemptDTO dto) {
        // 1. Validate Test
        JlptTest test = testRepository.findById(dto.getTestId())
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        if (!test.getIsPublished()) {
            throw new BadRequestException("Cannot submit to unpublished test");
        }

        // 2. Parse user answers
        List<UserAnswerItem> userAnswers;
        try {
            userAnswers = objectMapper.readValue(dto.getUserAnswers(), new TypeReference<List<UserAnswerItem>>() {
            });
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Invalid user answers format");
        }

        // 3. Load all questions for scoring (Map for fast lookup)
        List<JlptQuestion> questions = questionRepository.findByTestIdOrderByQuestionOrder(dto.getTestId());
        Map<Long, JlptQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(JlptQuestion::getId, Function.identity()));

        // 4. Calculate Scores
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal languageKnowledgeScore = BigDecimal.ZERO;
        BigDecimal readingScore = BigDecimal.ZERO;
        BigDecimal listeningScore = BigDecimal.ZERO;
        int correctCount = 0;

        List<UserAnswerResult> resultList = new java.util.ArrayList<>();

        for (UserAnswerItem item : userAnswers) {
            JlptQuestion q = questionMap.get(item.getQuestionId());
            if (q == null)
                continue; // Ignore answers for questions not in this test

            // Scoring logic: Compare selected with correctOption
            boolean isCorrect = false;
            if (q.getCorrectOption() != null && q.getCorrectOption().equals(item.getSelected())) {
                isCorrect = true;
                correctCount++;

                // Add points
                BigDecimal points = q.getPoints();
                totalScore = totalScore.add(points);

                // Add to section score
                switch (q.getSection()) {
                    case VOCABULARY:
                    case GRAMMAR:
                        languageKnowledgeScore = languageKnowledgeScore.add(points);
                        break;
                    case READING:
                        readingScore = readingScore.add(points);
                        break;
                    case LISTENING:
                        listeningScore = listeningScore.add(points);
                        break;
                }
            }

            // Record result detail
            resultList.add(new UserAnswerResult(
                    q.getId(),
                    item.getSelected(),
                    q.getCorrectOption(),
                    isCorrect));
        }

        // 5. Determine Pass/Fail (QUAN TRỌNG: Logic điểm liệt)
        boolean isPassed = totalScore.compareTo(BigDecimal.valueOf(test.getPassScore())) >= 0;

        // Check section pass scores (điểm liệt)
        // Lưu ý: test.getLanguageKnowledgePassScore() là int, cần convert
        if (languageKnowledgeScore.compareTo(BigDecimal.valueOf(test.getLanguageKnowledgePassScore())) < 0) {
            isPassed = false;
        }
        if (readingScore.compareTo(BigDecimal.valueOf(test.getReadingPassScore())) < 0) {
            isPassed = false;
        }
        if (listeningScore.compareTo(BigDecimal.valueOf(test.getListeningPassScore())) < 0) {
            isPassed = false;
        }

        // 6. Save Attempt
        try {
            JlptTestAttempt attempt = JlptTestAttempt.builder()
                    .user(User.builder().id(userId).build()) // Assume user exists/managed by security context
                    .test(test)
                    .totalScore(totalScore)
                    .score(totalScore) // Set score identical to totalScore (REQUIRED by DB)
                    .maxScore(test.getMaxScore()) // Set max score from test (REQUIRED by DB)
                    .percentage(test.getMaxScore() > 0
                            ? totalScore.multiply(BigDecimal.valueOf(100))
                                    .divide(BigDecimal.valueOf(test.getMaxScore()), 2, java.math.RoundingMode.HALF_UP)
                            : BigDecimal.ZERO)
                    .isPassed(isPassed)
                    .languageKnowledgeScore(languageKnowledgeScore)
                    .readingScore(readingScore)
                    .listeningScore(listeningScore)
                    .correctAnswers(correctCount)
                    .totalQuestions(questions.size()) // Size of all questions (including parents?) -> No, actually
                                                      // should filter only child questions?
                    // Wait, parent doesn't have score. So usually totalQuestions count should be
                    // child only.
                    // But questionRepository.findByTestId... returns both parent and children.
                    // Let's refine totalQuestions count:
                    .totalQuestions((int) questions.stream().filter(q -> q.getCorrectOption() != null).count())
                    .timeSpent(dto.getTimeSpent())
                    .userAnswers(objectMapper.writeValueAsString(resultList))
                    .answers(objectMapper.writeValueAsString(resultList)) // Populate duplicate field
                    .build();

            JlptTestAttempt savedAttempt = attemptRepository.save(attempt);

            // Update test stats (async usually, but sync for now)
            updateTestStats(test);

            return convertToResponse(savedAttempt);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing answers", e);
        }
    }

    public TestAttemptResponseDTO getAttemptById(Long id) {
        JlptTestAttempt attempt = attemptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test attempt not found with ID: " + id));
        return convertToResponse(attempt);
    }

    @Transactional(readOnly = true)
    public List<TestAttemptResponseDTO> getAttemptsByUserId(Long userId) {
        List<JlptTestAttempt> attempts = attemptRepository.findByUserIdOrderByStartedAtDesc(userId);
        return attempts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private void updateTestStats(JlptTest test) {
        // Simple increment logic
        test.setAttemptCount(test.getAttemptCount() + 1);
        // Recalculate average? (Optional optimization: do batch later)
        testRepository.save(test);
    }

    private TestAttemptResponseDTO convertToResponse(JlptTestAttempt attempt) {
        JlptTest test = attempt.getTest();
        JlptTestResponseDTO testDTO = null;
        if (test != null) {
            testDTO = JlptTestResponseDTO.builder()
                    .id(test.getId())
                    .title(test.getTitle())
                    .level(test.getLevel())
                    .testType(test.getTestType())
                    .duration(test.getDuration())
                    .maxScore(test.getMaxScore())
                    .passScore(test.getPassScore())
                    .build();
        }

        return TestAttemptResponseDTO.builder()
                .id(attempt.getId())
                .userId(attempt.getUser().getId())
                .testId(attempt.getTest().getId())
                .test(testDTO)
                .totalScore(attempt.getTotalScore())
                .isPassed(attempt.getIsPassed())
                .languageKnowledgeScore(attempt.getLanguageKnowledgeScore())
                .readingScore(attempt.getReadingScore())
                .listeningScore(attempt.getListeningScore())
                .correctAnswers(attempt.getCorrectAnswers())
                .totalQuestions(attempt.getTotalQuestions())
                .timeSpent(attempt.getTimeSpent())
                .userAnswers(attempt.getUserAnswers())
                .startedAt(attempt.getStartedAt())
                .completedAt(attempt.getCompletedAt())
                .build();
    }

    // Inner classes for JSON parsing
    @Data
    public static class UserAnswerItem {
        private Long questionId;
        private Integer selected;
    }

    @Data
    @RequiredArgsConstructor
    public static class UserAnswerResult {
        private final Long questionId;
        private final Integer selected;
        private final Integer correct;
        private final Boolean isCorrect;
    }
}
