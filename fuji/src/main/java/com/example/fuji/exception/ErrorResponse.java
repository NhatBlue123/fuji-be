package com.example.fuji.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;

    /**
     * i18n message key understood by the frontend (e.g. "auth.loginSuccess").
     * The backend MUST NOT send human-readable Vietnamese/English here.
     */
    private String messageKey;

    /**
     * Optional per-field error keys for validation (e.g. "auth.email.required").
     */
    private Map<String, String> errors; // For validation errors only

    // Constructor without validation errors
    public ErrorResponse(int status, String error, String messageKey) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.messageKey = messageKey;
    }

    // Constructor with validation errors
    public ErrorResponse(int status, String error, String messageKey, Map<String, String> errors) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.messageKey = messageKey;
        this.errors = errors;
    }
}


