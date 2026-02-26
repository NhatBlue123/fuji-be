package com.example.fuji.controller;

import com.example.fuji.dto.response.ApiResponse;
import com.example.fuji.dto.request.MediaDTO;

import com.example.fuji.dto.response.ImageSearchResponse;
import com.example.fuji.exception.BadRequestException;
import com.example.fuji.service.ImageSearchService;
import com.example.fuji.service.MediaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Media", description = "API quản lý media (upload/delete)")
@SecurityRequirement(name = "bearerAuth")
public class MediaController {

    private final MediaService mediaService;
    private final ImageSearchService imageSearchService;

    @PostMapping("/upload/image")
    public ResponseEntity<ApiResponse<MediaDTO>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        try {
            MediaDTO result = mediaService.uploadImage(file); // Changed type and removed toResponseDTO call
            return ResponseEntity.ok(ApiResponse.success("Tải ảnh lên thành công", result));
        } catch (Exception e) {
            log.error("Tải ảnh lên thất bại", e);
            throw new BadRequestException("Tải ảnh lên thất bại: " + e.getMessage());
        }
    }

    @PostMapping("/upload/video")
    public ResponseEntity<ApiResponse<MediaDTO>> uploadVideo(
            @RequestParam("file") MultipartFile file) {
        try {
            MediaDTO result = mediaService.uploadVideo(file); // Changed type and removed toResponseDTO call
            return ResponseEntity.ok(ApiResponse.success("Tải video lên thành công", result));
        } catch (Exception e) {
            log.error("Tải video lên thất bại", e);
            throw new BadRequestException("Tải video lên thất bại: " + e.getMessage());
        }
    }

    @PostMapping("/upload/audio")
    public ResponseEntity<ApiResponse<MediaDTO>> uploadAudio(
            @RequestParam("file") MultipartFile file) {
        try {
            MediaDTO result = mediaService.uploadAudio(file); // Changed type and removed toResponseDTO call
            return ResponseEntity.ok(ApiResponse.success("Tải audio lên thành công", result));
        } catch (Exception e) {
            log.error("Tải audio lên thất bại", e);
            throw new BadRequestException("Tải audio lên thất bại: " + e.getMessage());
        }
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> deleteMedia(
            @PathVariable String publicId,
            @RequestParam(value = "resourceType", defaultValue = "image") String resourceType) {
        try {
            mediaService.deleteMedia(publicId, resourceType);
            return ResponseEntity.ok(ApiResponse.success("Xóa media thành công"));
        } catch (Exception e) {
            log.error("Xóa media thất bại", e);
            throw new BadRequestException("Xóa media thất bại: " + e.getMessage());
        }
    }

    @GetMapping("/image-search")
    public ResponseEntity<ApiResponse<ImageSearchResponse>> searchImages(
            @RequestParam("q") String query,
            @RequestParam(value = "num", defaultValue = "10") int num) {
        ImageSearchResponse result = imageSearchService.search(query, num);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

}
