package com.example.fuji.service;

import com.cloudinary.Cloudinary;
import com.example.fuji.dto.request.MediaDTO;
import com.example.fuji.entity.MediaFile;
import com.example.fuji.entity.enums.ResourceType;
import com.example.fuji.repository.MediaFileRepository;
import com.example.fuji.utils.MediaValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

        private final Cloudinary cloudinary;
        private final MediaValidator mediaValidator;
        private final MediaFileRepository mediaFileRepository;

        @Transactional
        @SuppressWarnings("unchecked")
        public MediaDTO uploadImage(MultipartFile file) throws IOException {
                mediaValidator.validate(file);
                mediaValidator.isImage(file);

                byte[] optimizedImage = optimizeImage(file);

                // Lấy thông tin kích thước ảnh
                BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
                int width = bufferedImage != null ? bufferedImage.getWidth() : 0;
                int height = bufferedImage != null ? bufferedImage.getHeight() : 0;

                Map<String, Object> uploadResult = cloudinary.uploader().upload(optimizedImage, Map.of(
                                "resource_type", "image",
                                "public_id", generateUniqueId(),
                                "folder", "fuji/images"));

                // Lưu vào DB và convert sang DTO
                MediaFile savedFile = saveMediaFile(uploadResult, ResourceType.IMAGE, file.getOriginalFilename(), width,
                                height, null);
                return toMediaDTO(savedFile);
        }

        @Transactional
        @SuppressWarnings("unchecked")
        public MediaDTO uploadVideo(MultipartFile file) throws IOException { // Reverted to MediaDTO
                mediaValidator.validate(file);
                mediaValidator.isVideo(file);

                try (InputStream is = file.getInputStream()) {
                        Map<String, Object> uploadResult = cloudinary.uploader().upload(is, Map.of(
                                        "resource_type", "video",
                                        "public_id", generateUniqueId(),
                                        "folder", "fuji/videos",
                                        "eager", List.of(Map.of(
                                                        "width", 1000,
                                                        "crop", "scale",
                                                        "quality", "auto:best",
                                                        "fetch_format", "auto")),
                                        "eager_async", true));

                        // Lưu vào DB (Video có thể có duration, nhưng Cloudinary trả về duration trong
                        // response)
                        Double duration = uploadResult.containsKey("duration")
                                        ? Double.parseDouble(uploadResult.get("duration").toString())
                                        : 0.0;

                        Integer width = uploadResult.containsKey("width") ? (Integer) uploadResult.get("width") : 0;
                        Integer height = uploadResult.containsKey("height") ? (Integer) uploadResult.get("height") : 0;

                        MediaFile savedFile = saveMediaFile(uploadResult, ResourceType.VIDEO,
                                        file.getOriginalFilename(), width, height, BigDecimal.valueOf(duration));
                        return toMediaDTO(savedFile);
                }
        }

        @Transactional
        @SuppressWarnings("unchecked")
        public MediaDTO uploadAudio(MultipartFile file) throws IOException { // Reverted to MediaDTO
                mediaValidator.validate(file);
                mediaValidator.isAudio(file);
                try (InputStream is = file.getInputStream()) {
                        byte[] audioBytes = is.readAllBytes(); // Cloudinary needs byte[], not InputStream
                        Map<String, Object> uploadResult = cloudinary.uploader().upload(audioBytes, Map.of(
                                        "resource_type", "video", // Cloudinary xử lý audio như video
                                        "public_id", generateUniqueId(),
                                        "folder", "fuji/audios"));

                        // Cloudinary trả về duration
                        Double duration = uploadResult.containsKey("duration")
                                        ? Double.parseDouble(uploadResult.get("duration").toString())
                                        : 0.0;

                        MediaFile savedFile = saveMediaFile(uploadResult, ResourceType.AUDIO,
                                        file.getOriginalFilename(), null, null, BigDecimal.valueOf(duration));
                        return toMediaDTO(savedFile);
                }
        }

        @Transactional
        public void deleteMedia(String publicId) throws Exception {
                // Tìm trong DB để lấy resource_type
                MediaFile mediaFile = mediaFileRepository.findByCloudinaryPublicId(publicId)
                                .orElseThrow(() -> new RuntimeException("Media not found: " + publicId));

                String resourceType = mediaFile.getResourceType() == ResourceType.AUDIO ? "video"
                                : mediaFile.getResourceType().name().toLowerCase();

                cloudinary.uploader().destroy(publicId, Map.of("resource_type", resourceType));

                // Xóa trong DB
                mediaFileRepository.delete(mediaFile);

                log.info("Xóa media thành công: {}", publicId);
        }

        private byte[] optimizeImage(MultipartFile file) throws IOException {
                BufferedImage input = ImageIO.read(file.getInputStream());
                ByteArrayOutputStream output = new ByteArrayOutputStream();

                Thumbnails.of(input)
                                .size(800, 800) // Tăng size lên chút cho rõ
                                .outputFormat("jpeg")
                                .outputQuality(0.8)
                                .toOutputStream(output);

                return output.toByteArray();
        }

        private String generateUniqueId() {
                String uniquePart = UUID.randomUUID().toString().substring(0, 9);
                return "media_" + System.currentTimeMillis() + "_" + uniquePart;
        }

        private MediaFile saveMediaFile(Map<String, Object> uploadResult, ResourceType resourceType,
                        String originalFilename, Integer width, Integer height, BigDecimal duration) {
                MediaFile mediaFile = MediaFile.builder()
                                .cloudinaryPublicId((String) uploadResult.get("public_id"))
                                .cloudinaryUrl((String) uploadResult.get("secure_url"))
                                .resourceType(resourceType)
                                .format((String) uploadResult.get("format"))
                                .fileSize(((Number) uploadResult.get("bytes")).longValue())
                                .originalFilename(originalFilename)
                                .width(width)
                                .height(height)
                                .duration(duration)
                                .build();

                return mediaFileRepository.save(mediaFile);
        }

        // Helper method để convert sang DTO cũ nếu cần (cho backward compatibility)
        public MediaDTO toMediaDTO(MediaFile mediaFile) {
                return MediaDTO.builder()
                                .id(mediaFile.getId()) // Populate ID
                                .url(mediaFile.getCloudinaryUrl())
                                .publicId(mediaFile.getCloudinaryPublicId())
                                .resourceType(mediaFile.getResourceType().name().toLowerCase())
                                .size(mediaFile.getFileSize())
                                .format(mediaFile.getFormat())
                                .build();
        }
}
