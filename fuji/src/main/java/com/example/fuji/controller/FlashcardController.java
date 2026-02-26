package com.example.fuji.controller;

import com.example.fuji.dto.response.ApiResponse;
import com.example.fuji.entity.Flashcard;
import com.example.fuji.service.FlashcardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
@Tag(name = "Flashcards", description = "API quản lý Flashcards")
@SecurityRequirement(name = "bearerAuth")
public class FlashcardController {

    private final FlashcardService flashcardService;

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    @Operation(summary = "Import Flashcards từ Excel (.xlsx)")
    public ResponseEntity<ApiResponse<String>> importFlashcards(
            @RequestPart("file") MultipartFile file,
            @RequestPart("lesson") String lesson) {
        flashcardService.importFromExcel(file, lesson);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Import flashcards thành công")
                .data("Imported successfully")
                .build());
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả flashcards")
    public ResponseEntity<ApiResponse<List<Flashcard>>> getAllFlashcards() {
        return ResponseEntity.ok(ApiResponse.<List<Flashcard>>builder()
                .success(true)
                .message("Lấy danh sách flashcards thành công")
                .data(flashcardService.getAllFlashcards())
                .build());
    }
}
