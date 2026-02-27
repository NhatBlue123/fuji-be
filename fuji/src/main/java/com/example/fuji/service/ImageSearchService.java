package com.example.fuji.service;

import com.example.fuji.dto.response.ImageSearchResponse;
import com.example.fuji.dto.response.ImageSearchResponse.ImageItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Service for searching images via Serper.dev Images API.
 * Restricts results to www.irasutoya.com.
 */
@Service
@Slf4j
public class ImageSearchService {

    private static final String SERPER_IMAGES_URL = "https://google.serper.dev/images";
    private static final String SITE_FILTER = "site:www.irasutoya.com";

    @Value("${serper.api-key:}")
    private String serperApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

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
