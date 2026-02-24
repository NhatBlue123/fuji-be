package com.example.fuji.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.fuji.entity.MediaFile;
import com.example.fuji.entity.enums.ResourceType;

/**
 * Repository để truy vấn bảng media_files
 */
@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {

    /**
     * Tìm file theo cloudinary_public_id
     * 
     * @param publicId Public ID trên Cloudinary
     * @return Optional<MediaFile>
     */
    Optional<MediaFile> findByCloudinaryPublicId(String publicId);

    /**
     * Kiểm tra file có tồn tại theo public_id không
     * 
     * @param publicId Public ID trên Cloudinary
     * @return true nếu tồn tại
     */
    boolean existsByCloudinaryPublicId(String publicId);

    /**
     * Tìm tất cả file theo loại (IMAGE, AUDIO, VIDEO, RAW)
     * 
     * @param resourceType Loại resource
     * @return Danh sách MediaFile
     */
    List<MediaFile> findByResourceType(ResourceType resourceType);

    /**
     * Tìm tất cả file do một user upload
     * 
     * @param userId ID của user
     * @return Danh sách MediaFile
     */
    @Query("SELECT m FROM MediaFile m WHERE m.uploadedBy.id = :userId ORDER BY m.createdAt DESC")
    List<MediaFile> findByUploadedByUserId(@Param("userId") Long userId);

    /**
     * Tìm các file orphan (không được dùng bởi bất kỳ câu hỏi nào)
     * Dùng để cleanup
     * 
     * @return Danh sách MediaFile không được dùng
     */
    @Query("SELECT m FROM MediaFile m WHERE " +
            "m.id NOT IN (SELECT q.imageMedia.id FROM JlptQuestion q WHERE q.imageMedia IS NOT NULL) " +
            "AND m.id NOT IN (SELECT q.audioMedia.id FROM JlptQuestion q WHERE q.audioMedia IS NOT NULL)")
    List<MediaFile> findOrphanFiles();
}
