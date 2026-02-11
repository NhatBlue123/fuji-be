package com.example.fuji.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.fuji.dto.request.CardDTO;
import com.example.fuji.dto.request.FlashCardRequestDTO;
import com.example.fuji.dto.request.FlashCardUpdateDTO;
import com.example.fuji.dto.request.MediaDTO;
import com.example.fuji.dto.response.CardResponseDTO;
import com.example.fuji.dto.response.FlashCardResponseDTO;
import com.example.fuji.dto.response.PaginationDTO;
import com.example.fuji.dto.response.UserStudyProgressDTO;
import com.example.fuji.dto.response.UserSummaryDTO;
import com.example.fuji.entity.Card;
import com.example.fuji.entity.FlashCard;
import com.example.fuji.entity.User;
import com.example.fuji.entity.UserFlashCardStudy;
import com.example.fuji.enums.JlptLevel;
import com.example.fuji.exception.ResourceNotFoundException;
import com.example.fuji.repository.CardRepository;
import com.example.fuji.repository.FlashCardRepository;
import com.example.fuji.repository.FlashListCardRepository;
import com.example.fuji.repository.UserFlashCardStudyRepository;
import com.example.fuji.utils.AuthUtils;

import lombok.RequiredArgsConstructor;

/**
 * Service quản lý FlashCard (bộ thẻ).
 * CRUD operations, quản lý thẻ con (Card), tìm kiếm theo query/level/select.
 */
@Service
@RequiredArgsConstructor
public class FlashCardService {

    private final FlashCardRepository flashCardRepository;
    private final CardRepository cardRepository;
    private final FlashListCardRepository flashListCardRepository;
    private final UserFlashCardStudyRepository userFlashCardStudyRepository;
    private final MediaService mediaService;
    private final AuthUtils authUtils;

    @Transactional(readOnly = true)
    public Page<FlashCardResponseDTO> getAllFlashCards(int page, int limit) {
        User currentUser = authUtils.getCurrentUser();
        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());
        Page<FlashCard> flashCards = flashCardRepository.findAllAccessible(currentUser.getId(), pageable);
        return flashCards.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public FlashCardResponseDTO getFlashCardById(Long cardId) {
        FlashCard flashCard = flashCardRepository.findByIdAndDeletedAtIsNull(cardId)
            .orElseThrow(() -> new ResourceNotFoundException("FlashCard không tồn tại với id: " + cardId));
        return convertToDTO(flashCard);
    }

    @Transactional
    public FlashCardResponseDTO createFlashCard(FlashCardRequestDTO dto, MultipartFile thumbnail) {
        User currentUser = authUtils.getCurrentUser();

        String thumbnailUrl = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                MediaDTO uploadResult = mediaService.uploadImage(thumbnail);
                thumbnailUrl = uploadResult.getUrl();
            } catch (IOException e) {
                throw new RuntimeException("Upload ảnh thumbnail thất bại: " + e.getMessage(), e);
            }
        }

        FlashCard flashCard = FlashCard.builder()
            .user(currentUser)
            .name(dto.getName())
            .description(dto.getDescription())
            .level(dto.getLevel())
            .thumbnailUrl(thumbnailUrl)
            .isPublic(dto.getIsPublic() != null ? dto.getIsPublic() : true)
            .build();

        FlashCard savedFlashCard = flashCardRepository.save(flashCard);

        if (dto.getCards() != null && !dto.getCards().isEmpty()) {
            List<Card> cards = IntStream.range(0, dto.getCards().size())
                .mapToObj(i -> {
                    CardDTO cardDTO = dto.getCards().get(i);
                    return Card.builder()
                        .flashCard(savedFlashCard)
                        .vocabulary(cardDTO.getVocabulary())
                        .meaning(cardDTO.getMeaning())
                        .pronunciation(cardDTO.getPronunciation())
                        .exampleSentence(cardDTO.getExampleSentence())
                        .previewUrl(cardDTO.getPreviewUrl())
                        .cardOrder(i)
                        .build();
                })
                .collect(Collectors.toList());
            cardRepository.saveAll(cards);
            savedFlashCard.setCardCount(cards.size());
            flashCardRepository.save(savedFlashCard);
        }

        return convertToDTO(flashCardRepository.findById(savedFlashCard.getId()).orElseThrow());
    }

    @Transactional
    public FlashCardResponseDTO updateFlashCard(Long cardId, FlashCardUpdateDTO dto, MultipartFile thumbnail) {
        FlashCard flashCard = flashCardRepository.findByIdAndDeletedAtIsNull(cardId)
            .orElseThrow(() -> new ResourceNotFoundException("FlashCard không tồn tại với id: " + cardId));

        User currentUser = authUtils.getCurrentUser();
        if (!flashCard.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa FlashCard này");
        }

        if (dto.getName() != null && !dto.getName().isBlank()) {
            flashCard.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            flashCard.setDescription(dto.getDescription());
        }
        if (dto.getLevel() != null) {
            flashCard.setLevel(dto.getLevel());
        }
        if (dto.getIsPublic() != null) {
            flashCard.setIsPublic(dto.getIsPublic());
        }

        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                MediaDTO uploadResult = mediaService.uploadImage(thumbnail);
                flashCard.setThumbnailUrl(uploadResult.getUrl());
            } catch (IOException e) {
                throw new RuntimeException("Upload ảnh thumbnail thất bại: " + e.getMessage(), e);
            }
        }

        if (dto.getCards() != null) {
            cardRepository.deleteByFlashCardId(cardId);
            List<Card> cards = IntStream.range(0, dto.getCards().size())
                .mapToObj(i -> {
                    CardDTO cardDTO = dto.getCards().get(i);
                    return Card.builder()
                        .flashCard(flashCard)
                        .vocabulary(cardDTO.getVocabulary())
                        .meaning(cardDTO.getMeaning())
                        .pronunciation(cardDTO.getPronunciation())
                        .exampleSentence(cardDTO.getExampleSentence())
                        .previewUrl(cardDTO.getPreviewUrl())
                        .cardOrder(i)
                        .build();
                })
                .collect(Collectors.toList());
            cardRepository.saveAll(cards);
            flashCard.setCardCount(cards.size());
        }

        FlashCard updatedFlashCard = flashCardRepository.save(flashCard);
        return convertToDTO(updatedFlashCard);
    }

    @Transactional
    public void deleteFlashCard(Long cardId) {
        FlashCard flashCard = flashCardRepository.findByIdAndDeletedAtIsNull(cardId)
            .orElseThrow(() -> new ResourceNotFoundException("FlashCard không tồn tại với id: " + cardId));

        User currentUser = authUtils.getCurrentUser();
        if (!flashCard.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền xóa FlashCard này");
        }

        flashListCardRepository.deleteByFlashCardId(cardId);
        flashCard.setDeletedAt(LocalDateTime.now());
        flashCardRepository.save(flashCard);
    }

    @Transactional
    public FlashCardResponseDTO addCardToFlashCard(Long cardId, CardDTO cardDTO) {
        FlashCard flashCard = flashCardRepository.findByIdAndDeletedAtIsNull(cardId)
            .orElseThrow(() -> new ResourceNotFoundException("FlashCard không tồn tại với id: " + cardId));

        User currentUser = authUtils.getCurrentUser();
        if (!flashCard.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa FlashCard này");
        }

        int nextOrder = flashCard.getCardCount();
        Card card = Card.builder()
            .flashCard(flashCard)
            .vocabulary(cardDTO.getVocabulary())
            .meaning(cardDTO.getMeaning())
            .pronunciation(cardDTO.getPronunciation())
            .exampleSentence(cardDTO.getExampleSentence())
            .previewUrl(cardDTO.getPreviewUrl())
            .cardOrder(nextOrder)
            .build();
        cardRepository.save(card);

        flashCard.setCardCount(flashCard.getCardCount() + 1);
        flashCardRepository.save(flashCard);

        return convertToDTO(flashCard);
    }

    @Transactional
    public FlashCardResponseDTO deleteCardFromFlashCard(Long flashCardId, Integer cardIndex) {
        FlashCard flashCard = flashCardRepository.findByIdAndDeletedAtIsNull(flashCardId)
            .orElseThrow(() -> new ResourceNotFoundException("FlashCard không tồn tại với id: " + flashCardId));

        User currentUser = authUtils.getCurrentUser();
        if (!flashCard.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa FlashCard này");
        }

        List<Card> cards = cardRepository.findByFlashCardIdOrderByCardOrderAsc(flashCardId);
        if (cardIndex < 0 || cardIndex >= cards.size()) {
            throw new ResourceNotFoundException("Card index không hợp lệ: " + cardIndex);
        }

        Card cardToDelete = cards.get(cardIndex);
        cardRepository.delete(cardToDelete);

        for (int i = cardIndex + 1; i < cards.size(); i++) {
            cards.get(i).setCardOrder(i - 1);
        }
        cardRepository.saveAll(cards.subList(cardIndex + 1, cards.size()));

        flashCard.setCardCount(flashCard.getCardCount() - 1);
        flashCardRepository.save(flashCard);

        return convertToDTO(flashCard);
    }

    @Transactional(readOnly = true)
    public Page<FlashCardResponseDTO> searchFlashCards(String query, JlptLevel level, String select, int page, int limit) {
        User currentUser = authUtils.getCurrentUser();
        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());

        String selectValue = select != null ? select.toLowerCase() : "all";
        if (!selectValue.equals("me") && !selectValue.equals("other") && !selectValue.equals("all")) {
            selectValue = "all";
        }

        Page<FlashCard> flashCards = flashCardRepository.search(query, level, selectValue, currentUser.getId(), pageable);
        return flashCards.map(this::convertToDTO);
    }

    /**
     * Bắt đầu học một FlashCard.
     * Nếu user chưa có trong danh sách học, thêm vào.
     * Trả về thông tin tiến độ học của user.
     */
    @Transactional
    public UserStudyProgressDTO startLearning(Long flashCardId) {
        User currentUser = authUtils.getCurrentUser();
        FlashCard flashCard = flashCardRepository.findByIdAndDeletedAtIsNull(flashCardId)
            .orElseThrow(() -> new ResourceNotFoundException("FlashCard không tồn tại với id: " + flashCardId));

        // Check if already studying
        UserFlashCardStudy study = userFlashCardStudyRepository
            .findByUserIdAndFlashCardId(currentUser.getId(), flashCardId)
            .orElse(null);

        if (study == null) {
            // Create new study record
            study = UserFlashCardStudy.builder()
                .user(currentUser)
                .flashCard(flashCard)
                .progressPercentage(0)
                .rememberedCount(0)
                .totalCards(flashCard.getCardCount() != null ? flashCard.getCardCount() : 0)
                .lastStudiedAt(LocalDateTime.now())
                .nextReviewAt(LocalDateTime.now().plusDays(1))
                .isCompleted(false)
                .build();
            study = userFlashCardStudyRepository.save(study);
        } else {
            // Update last studied time
            study.setLastStudiedAt(LocalDateTime.now());
            userFlashCardStudyRepository.save(study);
        }

        return UserStudyProgressDTO.builder()
            .id(study.getId())
            .progressPercentage(study.getProgressPercentage())
            .rememberedCount(study.getRememberedCount())
            .totalCards(study.getTotalCards())
            .lastStudiedAt(study.getLastStudiedAt())
            .nextReviewAt(study.getNextReviewAt())
            .isCompleted(study.getIsCompleted())
            .build();
    }

    /**
     * Nộp bài tập exercise và cập nhật tiến độ học.
     */
    @Transactional
    public void submitExerciseResult(Long flashCardId, String exerciseType, Integer correctCount, Integer totalCount) {
        User currentUser = authUtils.getCurrentUser();
        FlashCard flashCard = flashCardRepository.findByIdAndDeletedAtIsNull(flashCardId)
            .orElseThrow(() -> new ResourceNotFoundException("FlashCard không tồn tại với id: " + flashCardId));

        // Get or create study record
        UserFlashCardStudy study = userFlashCardStudyRepository
            .findByUserIdAndFlashCardId(currentUser.getId(), flashCardId)
            .orElse(null);

        if (study == null) {
            study = UserFlashCardStudy.builder()
                .user(currentUser)
                .flashCard(flashCard)
                .progressPercentage(0)
                .rememberedCount(0)
                .totalCards(flashCard.getCardCount() != null ? flashCard.getCardCount() : 0)
                .lastStudiedAt(LocalDateTime.now())
                .nextReviewAt(LocalDateTime.now().plusDays(1))
                .isCompleted(false)
                .build();
        }

        // Calculate new progress based on exercise results
        int totalExercises = study.getTotalCards();
        int newRememberedCount = Math.min(study.getRememberedCount() + correctCount, totalExercises);
        int newProgressPercentage = totalExercises > 0 ? (int) ((newRememberedCount * 100.0) / totalExercises) : 0;

        study.setRememberedCount(newRememberedCount);
        study.setProgressPercentage(newProgressPercentage);
        study.setLastStudiedAt(LocalDateTime.now());
        study.setNextReviewAt(LocalDateTime.now().plusDays(1));
        study.setIsCompleted(newProgressPercentage >= 100);

        userFlashCardStudyRepository.save(study);
    }

    private FlashCardResponseDTO convertToDTO(FlashCard flashCard) {
        UserSummaryDTO userSummary = UserSummaryDTO.builder()
            .id(flashCard.getUser().getId())
            .username(flashCard.getUser().getUsername())
            .fullName(flashCard.getUser().getFullName())
            .avatarUrl(flashCard.getUser().getAvatarUrl())
            .build();

        List<Card> cards = cardRepository.findByFlashCardIdOrderByCardOrderAsc(flashCard.getId());
        List<CardResponseDTO> cardDTOs = cards.stream()
            .map(card -> CardResponseDTO.builder()
                .id(card.getId())
                .vocabulary(card.getVocabulary())
                .meaning(card.getMeaning())
                .pronunciation(card.getPronunciation())
                .exampleSentence(card.getExampleSentence())
                .previewUrl(card.getPreviewUrl())
                .cardOrder(card.getCardOrder())
                .createdAt(card.getCreatedAt())
                .build())
            .collect(Collectors.toList());

        // Get study count
        Long studyCount = userFlashCardStudyRepository.countByFlashCardId(flashCard.getId());

        // Get current user's progress
        UserStudyProgressDTO userProgress = null;
        try {
            User currentUser = authUtils.getCurrentUser();
            UserFlashCardStudy study = userFlashCardStudyRepository
                .findByUserIdAndFlashCardId(currentUser.getId(), flashCard.getId())
                .orElse(null);
            if (study != null) {
                userProgress = UserStudyProgressDTO.builder()
                    .id(study.getId())
                    .progressPercentage(study.getProgressPercentage())
                    .rememberedCount(study.getRememberedCount())
                    .totalCards(study.getTotalCards())
                    .lastStudiedAt(study.getLastStudiedAt())
                    .nextReviewAt(study.getNextReviewAt())
                    .isCompleted(study.getIsCompleted())
                    .build();
            }
        } catch (Exception e) {
            // User not authenticated, skip progress
        }

        return FlashCardResponseDTO.builder()
            .id(flashCard.getId())
            .name(flashCard.getName())
            .description(flashCard.getDescription())
            .level(flashCard.getLevel())
            .thumbnailUrl(flashCard.getThumbnailUrl())
            .isPublic(flashCard.getIsPublic())
            .user(userSummary)
            .cards(cardDTOs)
            .cardCount(flashCard.getCardCount())
            .createdAt(flashCard.getCreatedAt())
            .updatedAt(flashCard.getUpdatedAt())
            .studyCount(studyCount)
            .userProgress(userProgress)
            .build();
    }

    public PaginationDTO createPagination(Page<?> page) {
        return PaginationDTO.builder()
            .page(page.getNumber())
            .limit(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }
}
