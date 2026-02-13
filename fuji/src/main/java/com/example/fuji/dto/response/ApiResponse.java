package com.example.fuji.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    @Builder.Default
    private boolean success = true;

    /**
     * i18n message key understood by the frontend (e.g. "auth.loginSuccess").
     * Backend MUST NOT put human-readable Vietnamese/English text here.
     */
    private String messageKey;

    private T data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .build();
    }

    public static <T> ApiResponse<T> success(String messageKey, T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .messageKey(messageKey)
            .data(data)
            .build();
    }

    public static <T> ApiResponse<T> success(String messageKey) {
        return ApiResponse.<T>builder()
            .success(true)
            .messageKey(messageKey)
            .build();
    }

}
