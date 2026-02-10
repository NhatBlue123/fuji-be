package com.example.fuji.controller;

import java.io.IOException;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.fuji.dto.request.CardDTO;
import com.example.fuji.dto.request.FlashCardRequestDTO;
import com.example.fuji.dto.request.FlashCardUpdateDTO;
import com.example.fuji.dto.response.ApiResponse;
import com.example.fuji.dto.response.FlashCardResponseDTO;
import com.example.fuji.dto.response.PaginationDTO;
import com.example.fuji.enums.JlptLevel;
import com.example.fuji.service.FlashCardService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
public class FlashCardController {

    private final FlashCardService flashCardService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getAllFlashCards(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {

        Page<FlashCardResponseDTO> flashCards = flashCardService.getAllFlashCards(page, limit);
        PaginationDTO pagination = flashCardService.createPagination(flashCards);

        return ResponseEntity.ok(ApiResponse.builder()
            .success(true)
            .message("Lấy danh sách FlashCard thành công")
            .data(java.util.Map.of(
                "flashCards", flashCards.getContent(),
                "pagination", pagination
            ))
            .build());
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<ApiResponse<FlashCardResponseDTO>> getFlashCardById(@PathVariable Long cardId) {
        FlashCardResponseDTO flashCard = flashCardService.getFlashCardById(cardId);
        return ResponseEntity.ok(ApiResponse.<FlashCardResponseDTO>builder()
            .success(true)
            .message("Lấy FlashCard thành công")
            .data(flashCard)
            .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FlashCardResponseDTO>> createFlashCard(
            @RequestPart("flashcard") String flashcardJson,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) throws IOException {

        FlashCardRequestDTO dto = objectMapper.readValue(flashcardJson, FlashCardRequestDTO.class);

        Set<ConstraintViolation<FlashCardRequestDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            StringBuilder errors = new StringBuilder();
            for (ConstraintViolation<FlashCardRequestDTO> violation : violations) {
                errors.append(violation.getMessage()).append("; ");
            }
            return ResponseEntity.badRequest().body(ApiResponse.<FlashCardResponseDTO>builder()
                .success(false)
                .message(errors.toString())
                .build());
        }

        FlashCardResponseDTO flashCard = flashCardService.createFlashCard(dto, thumbnail);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<FlashCardResponseDTO>builder()
            .success(true)
            .message("Tạo FlashCard thành công")
            .data(flashCard)
            .build());
    }

    @PutMapping("/{cardId}")
    public ResponseEntity<ApiResponse<FlashCardResponseDTO>> updateFlashCard(
            @PathVariable Long cardId,
            @RequestPart("flashcard") String flashcardJson,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) throws IOException {

        FlashCardUpdateDTO dto = objectMapper.readValue(flashcardJson, FlashCardUpdateDTO.class);
        FlashCardResponseDTO flashCard = flashCardService.updateFlashCard(cardId, dto, thumbnail);

        return ResponseEntity.ok(ApiResponse.<FlashCardResponseDTO>builder()
            .success(true)
            .message("Cập nhật FlashCard thành công")
            .data(flashCard)
            .build());
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<ApiResponse<Void>> deleteFlashCard(@PathVariable Long cardId) {
        flashCardService.deleteFlashCard(cardId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
            .success(true)
            .message("Xóa FlashCard thành công")
            .build());
    }

    @PostMapping("/{cardId}/items")
    public ResponseEntity<ApiResponse<FlashCardResponseDTO>> addCardToFlashCard(
            @PathVariable Long cardId,
            @Valid @RequestBody CardDTO cardDTO) {

        FlashCardResponseDTO flashCard = flashCardService.addCardToFlashCard(cardId, cardDTO);
        return ResponseEntity.ok(ApiResponse.<FlashCardResponseDTO>builder()
            .success(true)
            .message("Thêm thẻ vào FlashCard thành công")
            .data(flashCard)
            .build());
    }

    @DeleteMapping("/{cardId}/items/{cardIndex}")
    public ResponseEntity<ApiResponse<FlashCardResponseDTO>> deleteCardFromFlashCard(
            @PathVariable Long cardId,
            @PathVariable Integer cardIndex) {

        FlashCardResponseDTO flashCard = flashCardService.deleteCardFromFlashCard(cardId, cardIndex);
        return ResponseEntity.ok(ApiResponse.<FlashCardResponseDTO>builder()
            .success(true)
            .message("Xóa thẻ khỏi FlashCard thành công")
            .data(flashCard)
            .build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Object>> searchFlashCards(
            @RequestParam String query,
            @RequestParam(required = false) JlptLevel level,
            @RequestParam(defaultValue = "all") String select,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {

        Page<FlashCardResponseDTO> flashCards = flashCardService.searchFlashCards(query, level, select, page, limit);
        PaginationDTO pagination = flashCardService.createPagination(flashCards);

        return ResponseEntity.ok(ApiResponse.builder()
            .success(true)
            .message("Tìm kiếm FlashCard thành công")
            .data(java.util.Map.of(
                "results", flashCards.getContent(),
                "pagination", pagination
            ))
            .build());
    }
}
