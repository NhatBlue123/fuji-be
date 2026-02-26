package com.example.fuji.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.fuji.entity.converter.ResourceTypeConverter;
import com.example.fuji.entity.enums.ResourceType;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "media_files", indexes = {
        @Index(name = "idx_public_id", columnList = "cloudinary_public_id"),
        @Index(name = "idx_resource_type", columnList = "resource_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cloudinary_public_id", nullable = false, unique = true)
    private String cloudinaryPublicId;

    @Column(name = "cloudinary_url", nullable = false, length = 500)
    private String cloudinaryUrl;

    @Convert(converter = ResourceTypeConverter.class)
    @Column(name = "resource_type", nullable = false)
    private ResourceType resourceType;

    @Column(length = 10)
    private String format;

    @Column(name = "file_size")
    private Long fileSize;

    private Integer width;
    private Integer height;

    @Column(precision = 10, scale = 2)
    private BigDecimal duration;

    @Column(name = "original_filename")
    private String originalFilename;

    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}
