package com.example.fuji.util;

import com.example.fuji.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.Set;

@Component
public class MediaValidator {
    //chỉ cho phép tải lên các định dạng file này
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "mp4", "mov", "avi", "mkv",
        "jpg", "jpeg", "png", "gif",
        "mp3", "wav"
    );

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "audio/mpeg", "audio/wav",
        "video/mp4", "video/quicktime", "video/x-msvideo", "video/x-matroska",
        "image/jpeg", "image/png", "image/gif"
    );

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Yêu cầu file tải lên");
        }

        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        String extension = getExtension(filename);
        String mimeType = Optional.ofNullable(file.getContentType()).orElse("");

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("File không hợp lệ: " + extension);
        }

        if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new BadRequestException("MIME type không hợp lệ: " + mimeType);
        }
    }

    private String getExtension(String filename) {
        return filename.contains(".")
            ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase()
            : "";
    }

    public boolean isImage(MultipartFile file) {
        String mime = file.getContentType();
        return mime != null && mime.startsWith("image/");
    }

    public boolean isVideo(MultipartFile file) {
        String mime = file.getContentType();
        return mime != null && mime.startsWith("video/");
    }

    public boolean isAudio(MultipartFile file) {
        String mime = file.getContentType();
        return mime != null && mime.startsWith("audio/");
    }
}
