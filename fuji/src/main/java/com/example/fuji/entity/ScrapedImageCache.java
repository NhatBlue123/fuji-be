package com.example.fuji.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cache table for scraped images uploaded to Cloudinary.
 * Uses SHA-256 hash of the source URL as dedup key — if the same external image
 * URL is encountered again (same or different user), the existing Cloudinary copy
 * is reused instead of uploading a duplicate.
 */
@Entity
@Table(name = "scraped_image_cache", indexes = {
    @Index(name = "idx_sic_source_url_hash", columnList = "source_url_hash", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapedImageCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** SHA-256 hex digest of sourceUrl — used as unique lookup key */
    @Column(name = "source_url_hash", nullable = false, length = 64, unique = true)
    private String sourceUrlHash;

    /** Original external image URL (for traceability) */
    @Column(name = "source_url", nullable = false, columnDefinition = "TEXT")
    private String sourceUrl;

    /** Cloudinary public_id (e.g. "fuji/scraped/ab3f1c...") */
    @Column(name = "cloudinary_public_id", nullable = false, length = 255)
    private String cloudinaryPublicId;

    /** Cloudinary secure_url */
    @Column(name = "cloudinary_url", nullable = false, length = 500)
    private String cloudinaryUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
