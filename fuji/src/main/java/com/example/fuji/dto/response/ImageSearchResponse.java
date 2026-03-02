package com.example.fuji.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageSearchResponse {
    private List<ImageItem> items;
    private int totalResults;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageItem {
        private String imageUrl;
        private String thumbnailUrl;
        private String title;
        private int width;
        private int height;
        /** Cloudinary public_id (for management/deletion if needed) */
        private String cloudinaryPublicId;
        /** Original external source URL (for traceability) */
        private String sourceUrl;
    }
}
