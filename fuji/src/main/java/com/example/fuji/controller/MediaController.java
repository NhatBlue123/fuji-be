package com.example.fuji.controller;

import com.example.fuji.dto.request.MediaDTO;
import com.example.fuji.dto.response.ApiResponse;
import com.example.fuji.exception.BadRequestException;
import com.example.fuji.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Slf4j
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload/image")
    public ResponseEntity<ApiResponse<MediaDTO>> uploadImage(
        @RequestParam("file") MultipartFile file
    ) {
        try {
            MediaDTO result = mediaService.uploadImage(file);
            return ResponseEntity.ok(ApiResponse.success("Tải ảnh lên thành công", result));
        } catch (Exception e) {
            log.error("Tải ảnh lên thất bại", e);
            throw new BadRequestException("Tải ảnh lên thất bại: " + e.getMessage());
        }
    }

    @PostMapping("/upload/video")
    public ResponseEntity<ApiResponse<MediaDTO>> uploadVideo(
        @RequestParam("file") MultipartFile file
    ) {
        try {
            MediaDTO result = mediaService.uploadVideo(file);
            return ResponseEntity.ok(ApiResponse.success("Tải video lên thành công", result));
        } catch (Exception e) {
            log.error("Tải video lên thất bại", e);
            throw new BadRequestException("Tải video lên thất bại: " + e.getMessage());
        }
    }

    @PostMapping("/upload/audio")
    public ResponseEntity<ApiResponse<MediaDTO>> uploadAudio(
        @RequestParam("file") MultipartFile file
    ) {
        try {
            MediaDTO result = mediaService.uploadAudio(file);
            return ResponseEntity.ok(ApiResponse.success("Tải audio lên thành công", result));
        } catch (Exception e) {
            log.error("Tải audio lên thất bại", e);
            throw new BadRequestException("Tải audio lên thất bại: " + e.getMessage());
        }
    }

    @DeleteMapping("/{publicId}")
    public ResponseEntity<ApiResponse<Void>> deleteMedia(
        @PathVariable String publicId,
        @RequestParam String resourceType
    ) {
        try {
            mediaService.deleteMedia(publicId, resourceType);
            return ResponseEntity.ok(ApiResponse.success("Xóa media thành công"));
        } catch (Exception e) {
            log.error("Xóa media thất bại", e);
            throw new BadRequestException("Xóa media thất bại: " + e.getMessage());
        }
    }

}
