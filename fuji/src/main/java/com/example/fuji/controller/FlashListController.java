package com.example.fuji.controller;

import java.io.IOException;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.fuji.dto.request.FlashListRequestDTO;
import com.example.fuji.dto.request.FlashListUpdateDTO;
import com.example.fuji.dto.request.RatingRequestDTO;
import com.example.fuji.dto.response.ApiResponse;
import com.example.fuji.dto.response.FlashListPageDTO;
import com.example.fuji.dto.response.FlashListResponseDTO;
import com.example.fuji.dto.response.PaginationDTO;
import com.example.fuji.enums.JlptLevel;
import com.example.fuji.service.FlashListService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/flashlists")
@RequiredArgsConstructor
public class FlashListController {

    private final FlashListService flashListService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @GetMapping
    public ResponseEntity<ApiResponse<FlashListPageDTO>> getAllFlashLists(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {

        FlashListPageDTO result = flashListService.getAllFlashLists(page, limit);
        return ResponseEntity.ok(ApiResponse.<FlashListPageDTO>builder()
            .success(true)
            .message("Lấy danh sách FlashList thành công")
            .data(result)
            .build());
    }

    @GetMapping("/{listId}")
    public ResponseEntity<ApiResponse<FlashListResponseDTO>> getFlashListById(@PathVariable Long listId) {
        FlashListResponseDTO flashList = flashListService.getFlashListById(listId);
        return ResponseEntity.ok(ApiResponse.<FlashListResponseDTO>builder()
            .success(true)
            .message("Lấy FlashList thành công")
            .data(flashList)
            .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FlashListResponseDTO>> createFlashList(
            @RequestPart("flashlist") String flashlistJson,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) throws IOException {

        FlashListRequestDTO dto = objectMapper.readValue(flashlistJson, FlashListRequestDTO.class);

        Set<ConstraintViolation<FlashListRequestDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            StringBuilder errors = new StringBuilder();
            for (ConstraintViolation<FlashListRequestDTO> violation : violations) {
                errors.append(violation.getMessage()).append("; ");
            }
            return ResponseEntity.badRequest().body(ApiResponse.<FlashListResponseDTO>builder()
                .success(false)
                .message(errors.toString())
                .build());
        }

        FlashListResponseDTO flashList = flashListService.createFlashList(dto, thumbnail);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<FlashListResponseDTO>builder()
            .success(true)
            .message("Tạo FlashList thành công")
            .data(flashList)
            .build());
    }

    @PutMapping("/{listId}")
    public ResponseEntity<ApiResponse<FlashListResponseDTO>> updateFlashList(
            @PathVariable Long listId,
            @RequestPart("flashlist") String flashlistJson,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) throws IOException {

        FlashListUpdateDTO dto = objectMapper.readValue(flashlistJson, FlashListUpdateDTO.class);
        FlashListResponseDTO flashList = flashListService.updateFlashList(listId, dto, thumbnail);

        return ResponseEntity.ok(ApiResponse.<FlashListResponseDTO>builder()
            .success(true)
            .message("Cập nhật FlashList thành công")
            .data(flashList)
            .build());
    }

    @DeleteMapping("/{listId}")
    public ResponseEntity<ApiResponse<Void>> deleteFlashList(@PathVariable Long listId) {
        flashListService.deleteFlashList(listId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
            .success(true)
            .message("Xóa FlashList thành công")
            .build());
    }

    @DeleteMapping("/all/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteAllFlashLists(@PathVariable Long userId) {
        flashListService.deleteAllFlashLists(userId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
            .success(true)
            .message("Xóa tất cả FlashList của user thành công")
            .build());
    }

    @PostMapping("/{listId}/rate")
    public ResponseEntity<ApiResponse<FlashListResponseDTO>> rateFlashList(
            @PathVariable Long listId,
            @Valid @RequestBody RatingRequestDTO dto) {

        FlashListResponseDTO flashList = flashListService.rateFlashList(listId, dto);
        return ResponseEntity.ok(ApiResponse.<FlashListResponseDTO>builder()
            .success(true)
            .message("Đánh giá FlashList thành công")
            .data(flashList)
            .build());
    }

    @PostMapping("/{listId}/cards/{cardId}")
    public ResponseEntity<ApiResponse<FlashListResponseDTO>> addFlashCardToList(
            @PathVariable Long listId,
            @PathVariable Long cardId) {

        FlashListResponseDTO flashList = flashListService.addFlashCardToList(listId, cardId);
        return ResponseEntity.ok(ApiResponse.<FlashListResponseDTO>builder()
            .success(true)
            .message("Thêm FlashCard vào FlashList thành công")
            .data(flashList)
            .build());
    }

    @DeleteMapping("/{listId}/cards/{cardId}")
    public ResponseEntity<ApiResponse<FlashListResponseDTO>> removeFlashCardFromList(
            @PathVariable Long listId,
            @PathVariable Long cardId) {

        FlashListResponseDTO flashList = flashListService.removeFlashCardFromList(listId, cardId);
        return ResponseEntity.ok(ApiResponse.<FlashListResponseDTO>builder()
            .success(true)
            .message("Xóa FlashCard khỏi FlashList thành công")
            .data(flashList)
            .build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Object>> searchFlashLists(
            @RequestParam String query,
            @RequestParam(required = false) JlptLevel level,
            @RequestParam(defaultValue = "all") String select,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {

        Page<FlashListResponseDTO> flashLists = flashListService.searchFlashLists(query, level, select, page, limit);
        PaginationDTO pagination = flashListService.createPagination(flashLists);

        return ResponseEntity.ok(ApiResponse.builder()
            .success(true)
            .message("Tìm kiếm FlashList thành công")
            .data(java.util.Map.of(
                "results", flashLists.getContent(),
                "pagination", pagination
            ))
            .build());
    }
}
