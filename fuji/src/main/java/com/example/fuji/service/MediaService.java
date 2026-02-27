package com.example.fuji.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.example.fuji.dto.request.MediaDTO;
import com.example.fuji.utils.MediaValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

    private final Cloudinary cloudinary;
    private final MediaValidator mediaValidator;

    @SuppressWarnings("unchecked")
    public MediaDTO uploadImage(MultipartFile file) throws IOException {
        mediaValidator.validate(file);
        mediaValidator.isImage(file);

        byte[] optimizedImage = optimizeImage(file);

        Map<String, Object> uploadResult = cloudinary.uploader().upload(optimizedImage, Map.of(
                "resource_type", "image",
                "public_id", generateUniqueId(),
                "folder", "fuji/images"));

        log.info("Ảnh tải thành công: {}", uploadResult.get("secure_url"));
        return buildMediaDTO(uploadResult, "image");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public MediaDTO uploadVideo(MultipartFile file) throws IOException {
        mediaValidator.validate(file);
        mediaValidator.isVideo(file);

        Transformation eagerTransform = new Transformation()
                .width(1000).crop("scale").quality("auto:best").fetchFormat("auto");

        Map<String, Object> params = new java.util.HashMap<>();
        params.put("resource_type", "video");
        params.put("public_id", generateUniqueId());
        params.put("folder", "fuji/videos");
        params.put("eager", List.of(eagerTransform));
        params.put("eager_async", true);

        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

        log.info("Video tải thành công: {}", uploadResult.get("secure_url"));
        return buildMediaDTO(uploadResult, "video");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public MediaDTO uploadAudio(MultipartFile file) throws IOException {
        mediaValidator.validate(file);
        mediaValidator.isAudio(file);
        Transformation audioTransform = new Transformation()
                .quality("auto:best").fetchFormat("auto");

        Map<String, Object> params = new java.util.HashMap<>();
        params.put("resource_type", "auto");
        params.put("public_id", generateUniqueId());
        params.put("folder", "fuji/audios");
        params.put("format", "mp3");
        params.put("transformation", audioTransform);

        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

        log.info("Tải audio thành công: {}", uploadResult.get("secure_url"));
        return buildMediaDTO(uploadResult, "audio");
    }

    public void deleteMedia(String publicId, String resourceType) throws Exception {
        cloudinary.uploader().destroy(publicId, Map.of("resource_type", resourceType));
        log.info("Xóa media thành công: {} ({})", publicId, resourceType);
    }

    private byte[] optimizeImage(MultipartFile file) throws IOException {
        BufferedImage input = ImageIO.read(file.getInputStream());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        Thumbnails.of(input)
                .size(500, 500)
                .outputFormat("jpeg")
                .outputQuality(0.8)
                .toOutputStream(output);

        return output.toByteArray();
    }

    private String generateUniqueId() {
        String uniquePart = UUID.randomUUID().toString().substring(0, 9);
        return "media_" + System.currentTimeMillis() + "_" + uniquePart;
    }

    private MediaDTO buildMediaDTO(Map<String, Object> uploadResult, String resourceType) {
        return MediaDTO.builder()
                .url((String) uploadResult.get("secure_url"))
                .publicId((String) uploadResult.get("public_id"))
                .resourceType(resourceType)
                .size(((Number) uploadResult.get("bytes")).longValue())
                .format((String) uploadResult.get("format"))
                .build();
    }
}
