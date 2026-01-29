package com.example.fuji.exception;

/**
 * Exception cho unauthorized (401)
 * Sử dụng: throw new UnauthorizedException("Invalid token");
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
