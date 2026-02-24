package com.example.fuji.entity.enums;

/**
 * Enum định nghĩa các loại resource/media trong hệ thống
 * 
 * Dùng cho bảng media_files để phân loại file upload lên Cloudinary
 */
public enum ResourceType {
    IMAGE, // Ảnh: jpg, png, gif, webp
    AUDIO, // Âm thanh: mp3, wav, m4a
    VIDEO, // Video: mp4, avi, mov
    RAW // File khác: pdf, doc, txt
}
