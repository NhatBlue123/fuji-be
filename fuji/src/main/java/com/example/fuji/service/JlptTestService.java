package com.example.fuji.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fuji.dto.request.CreateJlptTestDTO;
import com.example.fuji.dto.request.CreateQuestionDTO;
import com.example.fuji.dto.request.MediaDTO;
import com.example.fuji.dto.request.UpdateJlptTestDTO;
import com.example.fuji.dto.request.UpdateQuestionDTO;
import com.example.fuji.dto.response.JlptTestResponseDTO;
import com.example.fuji.dto.response.QuestionResponseDTO;
import com.example.fuji.entity.JlptQuestion;
import com.example.fuji.entity.JlptTest;
import com.example.fuji.entity.MediaFile;
import com.example.fuji.entity.enums.JLPTLevel;
import com.example.fuji.entity.enums.QuestionType;
import com.example.fuji.exception.BadRequestException;
import com.example.fuji.exception.ResourceNotFoundException;
import com.example.fuji.repository.JlptQuestionRepository;
import com.example.fuji.repository.JlptTestRepository;
import com.example.fuji.repository.MediaFileRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service xử lý business logic cho hệ thống JLPT (New Structure)
 */
@Service
@RequiredArgsConstructor
public class JlptTestService {

    private final JlptTestRepository testRepository;
    private final JlptQuestionRepository questionRepository;
    private final MediaFileRepository mediaFileRepository;

    // ========================================================================
    // PHẦN 1: QUẢN LÝ ĐỀ THI (TEST MANAGEMENT)
    // ========================================================================

    @Transactional
    public JlptTestResponseDTO createTest(CreateJlptTestDTO dto) {
        JlptTest test = JlptTest.builder()
                .title(dto.getTitle())
                .level(dto.getLevel())
                .testType(dto.getTestType())
                .description(dto.getDescription())
                .duration(dto.getDuration())
                .totalQuestions(dto.getTotalQuestions())
                // Cấu hình điểm số
                .maxScore(dto.getMaxScore() != null ? dto.getMaxScore() : 180)
                .passScore(dto.getPassScore())
                .languageKnowledgePassScore(
                        dto.getLanguageKnowledgePassScore() != null ? dto.getLanguageKnowledgePassScore() : 19)
                .readingPassScore(dto.getReadingPassScore() != null ? dto.getReadingPassScore() : 19)
                .listeningPassScore(dto.getListeningPassScore() != null ? dto.getListeningPassScore() : 19)
                // Mặc định
                .isPublished(dto.getIsPublished() != null ? dto.getIsPublished() : false)
                .attemptCount(0)
                .averageScore(BigDecimal.ZERO)
                .build();

        JlptTest savedTest = testRepository.save(test);
        return convertToResponseDTO(savedTest, false);
    }

    @Transactional(readOnly = true)
    public Page<JlptTestResponseDTO> getAllTests(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return testRepository.findAll(pageable).map(test -> convertToResponseDTO(test, false));
    }

    @Transactional(readOnly = true)
    public JlptTestResponseDTO getTestById(Long id) {
        JlptTest test = testRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with ID: " + id));

        // Khi lấy chi tiết, load questions theo cấu trúc cây
        JlptTestResponseDTO dto = convertToResponseDTO(test, false); // convert cơ bản trước
        dto.setQuestions(getTreeStructuredQuestions(id)); // set questions dạng cây
        return dto;
    }

    // Tìm kiếm và lọc
    @Transactional(readOnly = true)
    public Page<JlptTestResponseDTO> getPublishedTestsByLevel(JLPTLevel level, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return testRepository.findPublishedByFilter(level, keyword, pageable)
                .map(test -> convertToResponseDTO(test, false));
    }

    @Transactional
    public JlptTestResponseDTO updateTest(Long id, UpdateJlptTestDTO dto) {
        JlptTest test = testRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with ID: " + id));

        // Update fields if present
        if (dto.getTitle() != null)
            test.setTitle(dto.getTitle());
        if (dto.getLevel() != null)
            test.setLevel(dto.getLevel());
        if (dto.getTestType() != null)
            test.setTestType(dto.getTestType());
        if (dto.getDescription() != null)
            test.setDescription(dto.getDescription());
        if (dto.getDuration() != null)
            test.setDuration(dto.getDuration());
        if (dto.getTotalQuestions() != null)
            test.setTotalQuestions(dto.getTotalQuestions());

        // Update score config
        if (dto.getMaxScore() != null)
            test.setMaxScore(dto.getMaxScore());
        if (dto.getPassScore() != null)
            test.setPassScore(dto.getPassScore());
        if (dto.getLanguageKnowledgePassScore() != null)
            test.setLanguageKnowledgePassScore(dto.getLanguageKnowledgePassScore());
        if (dto.getReadingPassScore() != null)
            test.setReadingPassScore(dto.getReadingPassScore());
        if (dto.getListeningPassScore() != null)
            test.setListeningPassScore(dto.getListeningPassScore());

        if (dto.getIsPublished() != null)
            test.setIsPublished(dto.getIsPublished());

        return convertToResponseDTO(testRepository.save(test), false);
    }

    @Transactional
    public void deleteTest(Long id) {
        if (!testRepository.existsById(id)) {
            throw new ResourceNotFoundException("Test not found with ID: " + id);
        }
        testRepository.deleteById(id);
    }

    // ========================================================================
    // PHẦN 2: QUẢN LÝ CÂU HỎI (QUESTION MANAGEMENT - PARENT/CHILD)
    // ========================================================================

    @Transactional
    public QuestionResponseDTO addQuestion(Long testId, CreateQuestionDTO dto) {
        JlptTest test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with ID: " + testId));

        // Xử lý Parent Question (nếu có)
        JlptQuestion parent = null;
        if (dto.getParentId() != null) {
            parent = questionRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent question not found with ID: " + dto.getParentId()));

            // Validate: Child question phải cùng test với parent
            if (!parent.getTest().getId().equals(testId)) {
                throw new BadRequestException("Parent question does not belong to this test");
            }
            // Validate: Không cho phép child làm cha của child khác (chỉ hỗ trợ 1 cấp)
            if (parent.getParent() != null) {
                throw new BadRequestException("Cannot create child of a child question (max depth is 1)");
            }
        }

        // Xử lý Media Linking
        MediaFile imageMedia = null;
        if (dto.getImageMediaId() != null) {
            imageMedia = mediaFileRepository.findById(dto.getImageMediaId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Image media not found: " + dto.getImageMediaId()));
        }

        MediaFile audioMedia = null;
        if (dto.getAudioMediaId() != null) {
            audioMedia = mediaFileRepository.findById(dto.getAudioMediaId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Audio media not found: " + dto.getAudioMediaId()));
        }

        JlptQuestion question = JlptQuestion.builder()
                .test(test)
                .mondaiNumber(dto.getMondaiNumber())
                .mondaiTitle(dto.getMondaiTitle())
                .parent(parent)
                .questionOrder(dto.getQuestionOrder())
                .section(dto.getSection())
                .questionType(QuestionType.MULTIPLE_CHOICE) // Default to multiple choice for JLPT
                .contentText(dto.getContentText())
                .contentTextDuplicate(dto.getContentText()) // Database has both columns
                .imageMedia(imageMedia)
                .audioMedia(audioMedia)
                // Child specific fields (parent thì null)
                .options(parent != null || dto.getParentId() != null ? dto.getOptions() : null)
                .correctOption(parent != null ? dto.getCorrectOption() : null)
                .explanation(parent != null ? dto.getExplanation() : null)
                .points(parent != null && dto.getPoints() != null ? dto.getPoints() : BigDecimal.ZERO) // Parent 0 điểm
                .build();

        // Validate logic: Nếu là child thì phải có options và correctOption
        if (parent != null) {
            if (dto.getOptions() == null)
                throw new BadRequestException("Child question must have options");
            if (dto.getCorrectOption() == null)
                throw new BadRequestException("Child question must have correct answer");
        }

        return convertQuestionToDTO(questionRepository.save(question));
    }

    @Transactional
    public QuestionResponseDTO updateQuestion(Long id, UpdateQuestionDTO dto) {
        JlptQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + id));

        if (dto.getMondaiNumber() != null)
            question.setMondaiNumber(dto.getMondaiNumber());
        if (dto.getMondaiTitle() != null)
            question.setMondaiTitle(dto.getMondaiTitle());
        if (dto.getQuestionOrder() != null)
            question.setQuestionOrder(dto.getQuestionOrder());
        if (dto.getSection() != null)
            question.setSection(dto.getSection());
        if (dto.getContentText() != null)
            question.setContentText(dto.getContentText());

        if (dto.getImageMediaId() != null) {
            MediaFile img = mediaFileRepository.findById(dto.getImageMediaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Image not found"));
            question.setImageMedia(img);
        }
        if (dto.getAudioMediaId() != null) {
            MediaFile audio = mediaFileRepository.findById(dto.getAudioMediaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Audio not found"));
            question.setAudioMedia(audio);
        }

        // Child fields
        if (dto.getOptions() != null)
            question.setOptions(dto.getOptions());
        if (dto.getCorrectOption() != null)
            question.setCorrectOption(dto.getCorrectOption());
        if (dto.getExplanation() != null)
            question.setExplanation(dto.getExplanation());
        if (dto.getPoints() != null)
            question.setPoints(dto.getPoints());

        return convertQuestionToDTO(questionRepository.save(question));
    }

    @Transactional
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Question not found with ID: " + id);
        }
        questionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<QuestionResponseDTO> getQuestionsByTestIdRaw(Long testId) {
        return questionRepository.findByTestIdOrderByQuestionOrder(testId).stream()
                .map(this::convertQuestionToDTO)
                .collect(Collectors.toList());
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Lấy danh sách câu hỏi dạng cây (Parent -> Children)
     */
    private List<QuestionResponseDTO> getTreeStructuredQuestions(Long testId) {
        // 1. Lấy tất cả câu hỏi của test
        List<JlptQuestion> allQuestions = questionRepository.findByTestIdOrderByQuestionOrder(testId);

        // 2. Tách Parent và Child
        Map<Long, List<JlptQuestion>> childrenMap = allQuestions.stream()
                .filter(q -> q.getParent() != null)
                .collect(Collectors.groupingBy(q -> q.getParent().getId()));

        List<JlptQuestion> parents = allQuestions.stream()
                .filter(q -> q.getParent() == null)
                .collect(Collectors.toList());

        // 3. Convert và ghép cây
        return parents.stream().map(parent -> {
            QuestionResponseDTO parentDTO = convertQuestionToDTO(parent);
            List<JlptQuestion> children = childrenMap.getOrDefault(parent.getId(), Collections.emptyList());

            // Sort children by order
            // (Mặc dù đã sort từ query nhưng group lại map có thể mất thứ tự, nên sort lại
            // cho chắc)
            children.sort((a, b) -> a.getQuestionOrder().compareTo(b.getQuestionOrder()));

            parentDTO.setChildren(children.stream()
                    .map(this::convertQuestionToDTO)
                    .collect(Collectors.toList()));
            return parentDTO;
        }).collect(Collectors.toList());
    }

    private JlptTestResponseDTO convertToResponseDTO(JlptTest test, boolean includeQuestions) {
        JlptTestResponseDTO dto = JlptTestResponseDTO.builder()
                .id(test.getId())
                .title(test.getTitle())
                .level(test.getLevel())
                .testType(test.getTestType())
                .description(test.getDescription())
                .duration(test.getDuration())
                .totalQuestions(test.getTotalQuestions())
                .maxScore(test.getMaxScore())
                .passScore(test.getPassScore())
                .languageKnowledgePassScore(test.getLanguageKnowledgePassScore())
                .readingPassScore(test.getReadingPassScore())
                .listeningPassScore(test.getListeningPassScore())
                .attemptCount(test.getAttemptCount())
                .averageScore(test.getAverageScore())
                .isPublished(test.getIsPublished())
                .createdAt(test.getCreatedAt())
                .updatedAt(test.getUpdatedAt())
                .build();
        return dto;
    }

    private QuestionResponseDTO convertQuestionToDTO(JlptQuestion q) {
        return QuestionResponseDTO.builder()
                .id(q.getId())
                .testId(q.getTest().getId())
                .mondaiNumber(q.getMondaiNumber())
                .mondaiTitle(q.getMondaiTitle())
                .parentId(q.getParent() != null ? q.getParent().getId() : null)
                .questionOrder(q.getQuestionOrder())
                .section(q.getSection())
                .contentText(q.getContentText())
                .imageMedia(q.getImageMedia() != null ? convertMediaToDTO(q.getImageMedia()) : null)
                .audioMedia(q.getAudioMedia() != null ? convertMediaToDTO(q.getAudioMedia()) : null)
                .options(q.getOptions())
                .correctOption(q.getCorrectOption())
                .explanation(q.getExplanation())
                .points(q.getPoints())
                .createdAt(q.getCreatedAt())
                .build();
    }

    private MediaDTO convertMediaToDTO(MediaFile media) {
        return MediaDTO.builder()
                .id(media.getId())
                .publicId(media.getCloudinaryPublicId())
                .url(media.getCloudinaryUrl())
                .resourceType(media.getResourceType().name().toLowerCase())
                .format(media.getFormat())
                .size(media.getFileSize())
                .build();
    }
}
