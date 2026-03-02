package com.example.fuji.service;

import com.example.fuji.dto.request.MediaDTO;
import com.example.fuji.dto.response.ImageSearchResponse;
import com.example.fuji.dto.response.ImageSearchResponse.ImageItem;
import com.example.fuji.entity.ScrapedImageCache;
import com.example.fuji.repository.ScrapedImageCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Service for searching images via Serper.dev Images API.
 * Restricts results to www.irasutoya.com.
 *
 * Two-phase flow:
 *   Phase 1 (search): Query Serper → return raw external URLs immediately (fast)
 *   Phase 2 (resolve): When user picks an image → check DB cache → upload to Cloudinary if needed → return Cloudinary URL
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImageSearchService {

    private static final String SERPER_IMAGES_URL = "https://google.serper.dev/images";
    private static final String SITE_FILTER = "site:www.irasutoya.com";

    @Value("${serper.api-key:}")
    private String serperApiKey;

    private final MediaService mediaService;
    private final ScrapedImageCacheRepository scrapedImageCacheRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    // ══════════════════════════════════════════════════════════
    // Phase 1: SEARCH — return raw external URLs (fast, no upload)
    // ══════════════════════════════════════════════════════════

    public ImageSearchResponse search(String query, int limit) {
        if (serperApiKey == null || serperApiKey.isBlank()) {
            log.warn("SERPER_API_KEY is not configured");
            return ImageSearchResponse.builder()
                    .items(Collections.emptyList())
                    .totalResults(0)
                    .build();
        }

        String fullQuery = query + " " + SITE_FILTER;
        int num = Math.min(Math.max(limit, 1), 100);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", serperApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("q", fullQuery);
        body.put("num", num);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    SERPER_IMAGES_URL, request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Serper API error: status={}", response.getStatusCode());
                return ImageSearchResponse.builder()
                        .items(Collections.emptyList())
                        .totalResults(0)
                        .build();
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> images = (List<Map<String, Object>>) response.getBody().get("images");
            if (images == null)
                images = Collections.emptyList();

            List<ImageItem> items = images.stream().map(img -> ImageItem.builder()
                    .imageUrl((String) img.getOrDefault("imageUrl", ""))
                    .thumbnailUrl((String) img.getOrDefault("thumbnailUrl",
                            img.getOrDefault("imageUrl", "")))
                    .title((String) img.getOrDefault("title", ""))
                    .width(toInt(img.get("imageWidth")))
                    .height(toInt(img.get("imageHeight")))
                    .build()).toList();

            return ImageSearchResponse.builder()
                    .items(items)
                    .totalResults(items.size())
                    .build();

        } catch (Exception e) {
            log.error("Image search failed for query '{}': {}", query, e.getMessage());
            return ImageSearchResponse.builder()
                    .items(Collections.emptyList())
                    .totalResults(0)
                    .build();
        }
    }

    // ══════════════════════════════════════════════════════════
    // Phase 2: RESOLVE — user picked 1 image, upload if not cached
    // ══════════════════════════════════════════════════════════

    /**
     * Resolve a single external image URL to a Cloudinary URL.
     * - If already cached in DB → return cached Cloudinary URL instantly
     * - If not cached → download, optimize, upload to Cloudinary, cache, return
     *
     * @param sourceUrl the raw external image URL the user selected
     * @return ResolvedImage with cloudinaryUrl and publicId
     */
    public ResolvedImage resolveImage(String sourceUrl) {
        String hash = MediaService.sha256(sourceUrl);

        // Check DB cache first
        Optional<ScrapedImageCache> cached = scrapedImageCacheRepository.findBySourceUrlHash(hash);
        if (cached.isPresent()) {
            ScrapedImageCache entry = cached.get();
            log.info("Cache hit for image: {} -> {}", sourceUrl, entry.getCloudinaryUrl());
            return new ResolvedImage(entry.getCloudinaryUrl(), entry.getCloudinaryPublicId(), sourceUrl, true);
        }

        // Cache miss — download + upload to Cloudinary
        try {
            MediaDTO uploaded = mediaService.uploadFromUrl(sourceUrl, hash);

            // Save to DB cache for future lookups
            ScrapedImageCache cacheEntry = ScrapedImageCache.builder()
                    .sourceUrlHash(hash)
                    .sourceUrl(sourceUrl)
                    .cloudinaryPublicId(uploaded.getPublicId())
                    .cloudinaryUrl(uploaded.getUrl())
                    .build();
            scrapedImageCacheRepository.save(cacheEntry);

            log.info("Resolved & uploaded image: {} -> {}", sourceUrl, uploaded.getUrl());
            return new ResolvedImage(uploaded.getUrl(), uploaded.getPublicId(), sourceUrl, false);
        } catch (Exception e) {
            log.error("Failed to resolve image '{}': {}", sourceUrl, e.getMessage());
            // Fallback: return the original URL so user experience isn't broken
            return new ResolvedImage(sourceUrl, null, sourceUrl, false);
        }
    }

    /** Result of resolving a single image */
    public record ResolvedImage(String cloudinaryUrl, String cloudinaryPublicId, String sourceUrl, boolean cacheHit) {}

    // ── Helpers ───────────────────────────────────────────────

    private int toInt(Object value) {
        if (value instanceof Number)
            return ((Number) value).intValue();
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }
}
