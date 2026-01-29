package com.example.fuji.exception;

/**
 * Exception cho bad request (400)
 * Sử dụng: throw new BadRequestException("Email already exists");
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
