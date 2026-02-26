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
import com.example.fuji.entity.enums.AttemptStatus;
import com.example.fuji.entity.JlptQuestion;
import com.example.fuji.entity.JlptTest;
import com.example.fuji.entity.JlptTestAttempt;
import com.example.fuji.entity.User;
import com.example.fuji.exception.AlreadySubmittedException;
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
    public TestAttemptResponseDTO startAttempt(Long userId, Long testId) {
        JlptTest test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        if (!test.getIsPublished()) {
            throw new BadRequestException("Cannot start an unpublished test");
        }

        // Check if there is already an IN_PROGRESS attempt
        List<JlptTestAttempt> inProgressAttempts = attemptRepository.findByUserIdAndTestIdAndStatus(userId, testId,
                AttemptStatus.IN_PROGRESS);

        // If there's an active attempt that hasn't expired, return it (resume)
        for (JlptTestAttempt attempt : inProgressAttempts) {
            if (attempt.getExpiresAt().isAfter(java.time.LocalDateTime.now())) {
                return convertToResponse(attempt);
            } else {
                // Expire the old stuck attempt
                attempt.setStatus(AttemptStatus.EXPIRED);
                attemptRepository.save(attempt);
            }
        }

        int durationMinutes = test.getDuration();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        JlptTestAttempt newAttempt = JlptTestAttempt.builder()
                .user(User.builder().id(userId).build())
                .test(test)
                .status(AttemptStatus.IN_PROGRESS)
                .durationMinutes(durationMinutes)
                .expiresAt(now.plusMinutes(durationMinutes))
                // Default placeholders for non-null fields strictly enforced by DB
                .totalScore(BigDecimal.ZERO)
                .score(BigDecimal.ZERO)
                .maxScore(test.getMaxScore() != null ? test.getMaxScore() : 180)
                .percentage(BigDecimal.ZERO)
                .isPassed(false)
                .correctAnswers(0)
                .totalQuestions(0)
                .timeSpent(0)
                .userAnswers("[]")
                .answers("[]")
                .build();

        JlptTestAttempt savedAttempt = attemptRepository.save(newAttempt);
        return convertToResponse(savedAttempt);
    }

    @Transactional
    public TestAttemptResponseDTO submitAttempt(Long userId, Long attemptId, SubmitTestAttemptDTO dto) {
        // 1. Validate Attempt & Test (Pessimistic Lock blocks concurrent threads here)
        JlptTestAttempt attempt = attemptRepository.findByIdWithLock(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam attempt not found"));

        if (!attempt.getUser().getId().equals(userId)) {
            throw new BadRequestException("You are not authorized to submit this attempt");
        }

        if (attempt.getStatus() == AttemptStatus.FINISHED) {
            throw new AlreadySubmittedException("Bài thi này đã được nộp hoặc xử lý trước đó.");
        }

        if (attempt.getStatus() == AttemptStatus.EXPIRED) {
            throw new BadRequestException("This exam attempt has expired");
        }

        // Anti-cheat: Validate Time
        if (java.time.LocalDateTime.now().isAfter(attempt.getExpiresAt().plusMinutes(1))) { // 1 min grace period
            attempt.setStatus(AttemptStatus.EXPIRED);
            attemptRepository.save(attempt);
            throw new BadRequestException("Exam time has expired. Submission rejected.");
        }

        JlptTest test = attempt.getTest();

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
            attempt.setStatus(AttemptStatus.FINISHED);
            attempt.setCompletedAt(java.time.LocalDateTime.now());
            attempt.setTotalScore(totalScore);
            attempt.setScore(totalScore); // Set score identical to totalScore (REQUIRED by DB)
            attempt.setMaxScore(test.getMaxScore()); // Set max score from test (REQUIRED by DB)
            attempt.setPercentage(test.getMaxScore() > 0
                    ? totalScore.multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(test.getMaxScore()), 2, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            attempt.setIsPassed(isPassed);
            attempt.setLanguageKnowledgeScore(languageKnowledgeScore);
            attempt.setReadingScore(readingScore);
            attempt.setListeningScore(listeningScore);
            attempt.setCorrectAnswers(correctCount);

            // Total questions filtering
            attempt.setTotalQuestions((int) questions.stream().filter(q -> q.getCorrectOption() != null).count());

            // Validate timeSpent against duration constraints if needed (Anti-cheat)
            int maxAllowedTimeInSeconds = attempt.getDurationMinutes() * 60;
            attempt.setTimeSpent(Math.min(dto.getTimeSpent(), maxAllowedTimeInSeconds));

            attempt.setUserAnswers(objectMapper.writeValueAsString(resultList));
            attempt.setAnswers(objectMapper.writeValueAsString(resultList));

            JlptTestAttempt savedAttempt = attemptRepository.save(attempt);

            // Update test stats (async usually, but sync for now)
            updateTestStats(test);

            return convertToResponse(savedAttempt);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing answers", e);
        }
    }

    /**
     * Convenience method: start (or resume) an attempt and immediately submit it.
     * Used by the frontend one-step POST /api/jlpt-tests/submit flow.
     * 
     * We flush after startAttempt so the new attempt row is visible to
     * submitAttempt's findByIdWithLock query within the same transaction.
     */
    @Transactional
    public TestAttemptResponseDTO startAndSubmitAttempt(Long userId, SubmitTestAttemptDTO dto) {
        // 1. Start (or resume) attempt — this saves the row to the DB within the
        // transaction
        TestAttemptResponseDTO started = startAttempt(userId, dto.getTestId());
        Long attemptId = started.getId();

        // 2. Flush so the pessimistic lock query in submitAttempt can see the new row
        attemptRepository.flush();

        // 3. Submit
        return submitAttempt(userId, attemptId, dto);
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
