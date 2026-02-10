package com.example.fuji.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.fuji.dto.request.FlashListRequestDTO;
import com.example.fuji.dto.request.FlashListUpdateDTO;
import com.example.fuji.dto.request.MediaDTO;
import com.example.fuji.dto.request.RatingRequestDTO;
import com.example.fuji.dto.response.FlashCardSummaryDTO;
import com.example.fuji.dto.response.FlashListPageDTO;
import com.example.fuji.dto.response.FlashListResponseDTO;
import com.example.fuji.dto.response.PaginationDTO;
import com.example.fuji.dto.response.UserSummaryDTO;
import com.example.fuji.entity.FlashCard;
import com.example.fuji.entity.FlashList;
import com.example.fuji.entity.FlashListCard;
import com.example.fuji.entity.FlashListRating;
import com.example.fuji.entity.User;
import com.example.fuji.enums.JlptLevel;
import com.example.fuji.exception.ResourceNotFoundException;
import com.example.fuji.repository.FlashCardRepository;
import com.example.fuji.repository.FlashListCardRepository;
import com.example.fuji.repository.FlashListRatingRepository;
import com.example.fuji.repository.FlashListRepository;
import com.example.fuji.utils.AuthUtils;

import lombok.RequiredArgsConstructor;

/**
 * Service quản lý FlashList (danh sách bộ thẻ).
 * CRUD operations, đánh giá (rating), thêm/xóa FlashCard, tìm kiếm.
 */
@Service
@RequiredArgsConstructor
public class FlashListService {

    private final FlashListRepository flashListRepository;
    private final FlashCardRepository flashCardRepository;
    private final FlashListCardRepository flashListCardRepository;
    private final FlashListRatingRepository flashListRatingRepository;
    private final MediaService mediaService;
    private final AuthUtils authUtils;

    @Transactional(readOnly = true)
    public FlashListPageDTO getAllFlashLists(int page, int limit) {
        User currentUser = authUtils.getCurrentUser();
        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());

        Page<FlashList> publicLists = flashListRepository.findByIsPublicTrueAndDeletedAtIsNull(pageable);
        Page<FlashList> myLists = flashListRepository.findByUserIdAndDeletedAtIsNull(currentUser.getId(), pageable);

        PaginationDTO pagination = PaginationDTO.builder()
            .page(page)
            .limit(limit)
            .totalElements(publicLists.getTotalElements() + myLists.getTotalElements())
            .totalPages(Math.max(publicLists.getTotalPages(), myLists.getTotalPages()))
            .hasNext(publicLists.hasNext() || myLists.hasNext())
            .hasPrevious(publicLists.hasPrevious() || myLists.hasPrevious())
            .build();

        return FlashListPageDTO.builder()
            .publicLists(publicLists.getContent().stream().map(this::convertToDTO).collect(Collectors.toList()))
            .myLists(myLists.getContent().stream().map(this::convertToDTO).collect(Collectors.toList()))
            .pagination(pagination)
            .build();
    }

    @Transactional(readOnly = true)
    public FlashListResponseDTO getFlashListById(Long listId) {
        FlashList flashList = flashListRepository.findByIdAndDeletedAtIsNull(listId)
            .orElseThrow(() -> new ResourceNotFoundException("FlashList không tồn tại với id: " + listId));
        return convertToDTO(flashList);
    }

    @Transactional
    public FlashListResponseDTO createFlashList(FlashListRequestDTO dto, MultipartFile thumbnail) {
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

        FlashList flashList = FlashList.builder()
            .user(currentUser)
            .title(dto.getTitle())
            .description(dto.getDescription())
            .level(dto.getLevel())
            .thumbnailUrl(thumbnailUrl)
            .isPublic(dto.getIsPublic() != null ? dto.getIsPublic() : true)
            .build();

        FlashList savedFlashList = flashListRepository.save(flashList);

        if (dto.getFlashcardIds() != null && !dto.getFlashcardIds().isEmpty()) {
            int order = 0;
            for (Long flashCardId : dto.getFlashcardIds()) {
                FlashCard flashCard = flashCardRepository.findByIdAndDeletedAtIsNull(flashCardId)
                    .orElseThrow(() -> new ResourceNotFoundException("FlashCard không tồn tại với id: " + flashCardId));

                FlashListCard flc = FlashListCard.builder()
                    .flashList(savedFlashList)
                    .flashCard(flashCard)
                    .cardOrder(order++)
                    .build();
                flashListCardRepository.save(flc);
            }
            savedFlashList.updateCardCount();
            flashListRepository.save(savedFlashList);
        }

        return convertToDTO(savedFlashList);
    }

    @Transactional
    public FlashListResponseDTO updateFlashList(Long listId, FlashListUpdateDTO dto, MultipartFile thumbnail) {
        FlashList flashList = flashListRepository.findByIdAndDeletedAtIsNull(listId)
            .orElseThrow(() -> new ResourceNotFoundException("FlashList không tồn tại với id: " + listId));

        User currentUser = authUtils.getCurrentUser();
        if (!flashList.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa FlashList này");
        }

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            flashList.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            flashList.setDescription(dto.getDescription());
        }
        if (dto.getLevel() != null) {
            flashList.setLevel(dto.getLevel());
        }
        if (dto.getIsPublic() != null) {
            flashList.setIsPublic(dto.getIsPublic());
        }

        if (thumbnail != null && !thumbnail.isEmpty()) {
            try {
                MediaDTO uploadResult = mediaService.uploadImage(thumbnail);
                flashList.setThumbnailUrl(uploadResult.getUrl());
            } catch (IOException e) {
                throw new RuntimeException("Upload ảnh thumbnail thất bại: " + e.getMessage(), e);
            }
        }

        FlashList updatedFlashList = flashListRepository.save(flashList);
        return convertToDTO(updatedFlashList);
    }

    @Transactional
    public void deleteFlashList(Long listId) {
        FlashList flashList = flashListRepository.findByIdAndDeletedAtIsNull(listId)
            .orElseThrow(() -> new ResourceNotFoundException("FlashList không tồn tại với id: " + listId));

        User currentUser = authUtils.getCurrentUser();
        if (!flashList.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền xóa FlashList này");
        }

        flashList.setDeletedAt(LocalDateTime.now());
        flashListRepository.save(flashList);
    }

    @Transactional
    public void deleteAllFlashLists(Long userId) {
        List<FlashList> lists = flashListRepository.findByUserIdAndDeletedAtIsNull(userId, Pageable.unpaged()).getContent();
        LocalDateTime now = LocalDateTime.now();
        for (FlashList list : lists) {
            list.setDeletedAt(now);
        }
        flashListRepository.saveAll(lists);
    }

    @Transactional
    public FlashListResponseDTO rateFlashList(Long listId, RatingRequestDTO dto) {
        FlashList flashList = flashListRepository.findByIdAndDeletedAtIsNull(listId)
            .orElseThrow(() -> new ResourceNotFoundException("FlashList không tồn tại với id: " + listId));

        User currentUser = authUtils.getCurrentUser();

        FlashListRating rating = flashListRatingRepository.findByUserIdAndListId(currentUser.getId(), listId)
            .orElse(FlashListRating.builder()
                .user(currentUser)
                .list(flashList)
                .build());

        rating.setRating(dto.getRating());
        flashListRatingRepository.save(rating);

        BigDecimal avgRating = flashListRatingRepository.getAverageRatingByListId(listId);
        Integer ratingCount = flashListRatingRepository.countByListId(listId);

        flashList.setAverageRating(avgRating != null ? avgRating.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        flashList.setRatingCount(ratingCount != null ? ratingCount : 0);
        flashListRepository.save(flashList);

        return convertToDTO(flashList);
    }

    @Transactional
    public FlashListResponseDTO addFlashCardToList(Long listId, Long cardId) {
        FlashList flashList = flashListRepository.findByIdAndDeletedAtIsNull(listId)
            .orElseThrow(() -> new ResourceNotFoundException("FlashList không tồn tại với id: " + listId));

        User currentUser = authUtils.getCurrentUser();
        if (!flashList.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa FlashList này");
        }

        FlashCard flashCard = flashCardRepository.findByIdAndDeletedAtIsNull(cardId)
            .orElseThrow(() -> new ResourceNotFoundException("FlashCard không tồn tại với id: " + cardId));

        if (flashListCardRepository.existsByFlashListIdAndFlashCardId(listId, cardId)) {
            throw new RuntimeException("FlashCard đã tồn tại trong FlashList");
        }

        int nextOrder = flashListCardRepository.countByFlashListId(listId);
        FlashListCard flc = FlashListCard.builder()
            .flashList(flashList)
            .flashCard(flashCard)
            .cardOrder(nextOrder)
            .build();
        flashListCardRepository.save(flc);

        flashList.updateCardCount();
        flashListRepository.save(flashList);

        return convertToDTO(flashList);
    }

    @Transactional
    public FlashListResponseDTO removeFlashCardFromList(Long listId, Long cardId) {
        FlashList flashList = flashListRepository.findByIdAndDeletedAtIsNull(listId)
            .orElseThrow(() -> new ResourceNotFoundException("FlashList không tồn tại với id: " + listId));

        User currentUser = authUtils.getCurrentUser();
        if (!flashList.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa FlashList này");
        }

        FlashListCard flc = flashListCardRepository.findByFlashListIdAndFlashCardId(listId, cardId)
            .orElseThrow(() -> new ResourceNotFoundException("FlashCard không tồn tại trong FlashList"));

        flashListCardRepository.delete(flc);
        flashList.updateCardCount();
        flashListRepository.save(flashList);

        return convertToDTO(flashList);
    }

    @Transactional(readOnly = true)
    public Page<FlashListResponseDTO> searchFlashLists(String query, JlptLevel level, String select, int page, int limit) {
        User currentUser = authUtils.getCurrentUser();
        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());

        String selectValue = select != null ? select.toLowerCase() : "all";
        if (!selectValue.equals("me") && !selectValue.equals("other") && !selectValue.equals("all")) {
            selectValue = "all";
        }

        Page<FlashList> flashLists = flashListRepository.search(query, level, selectValue, currentUser.getId(), pageable);
        return flashLists.map(this::convertToDTO);
    }

    private FlashListResponseDTO convertToDTO(FlashList flashList) {
        UserSummaryDTO userSummary = UserSummaryDTO.builder()
            .id(flashList.getUser().getId())
            .username(flashList.getUser().getUsername())
            .fullName(flashList.getUser().getFullName())
            .avatarUrl(flashList.getUser().getAvatarUrl())
            .build();

        List<FlashListCard> flashListCards = flashListCardRepository.findByFlashListIdOrderByCardOrderAsc(flashList.getId());
        List<FlashCardSummaryDTO> flashcards = flashListCards.stream()
            .map(flc -> {
                FlashCard fc = flc.getFlashCard();
                return FlashCardSummaryDTO.builder()
                    .id(fc.getId())
                    .name(fc.getName())
                    .level(fc.getLevel())
                    .thumbnailUrl(fc.getThumbnailUrl())
                    .cardCount(fc.getCardCount())
                    .build();
            })
            .collect(Collectors.toList());

        return FlashListResponseDTO.builder()
            .id(flashList.getId())
            .title(flashList.getTitle())
            .description(flashList.getDescription())
            .level(flashList.getLevel())
            .thumbnailUrl(flashList.getThumbnailUrl())
            .isPublic(flashList.getIsPublic())
            .user(userSummary)
            .flashcards(flashcards)
            .cardCount(flashList.getCardCount())
            .averageRating(flashList.getAverageRating())
            .ratingCount(flashList.getRatingCount())
            .studyCount(flashList.getStudyCount())
            .createdAt(flashList.getCreatedAt())
            .updatedAt(flashList.getUpdatedAt())
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
