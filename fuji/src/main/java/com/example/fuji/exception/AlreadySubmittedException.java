package com.example.fuji.exception;

public class AlreadySubmittedException extends RuntimeException {
    public AlreadySubmittedException(String message) {
        super(message);
    }
}
