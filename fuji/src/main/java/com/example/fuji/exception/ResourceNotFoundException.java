package com.example.fuji.exception;

/**
 * Exception khi không tìm thấy resource (404)
 * Sử dụng: throw new ResourceNotFoundException("User not found");
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
