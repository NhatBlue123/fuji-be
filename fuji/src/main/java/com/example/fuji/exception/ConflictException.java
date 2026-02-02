package com.example.fuji.exception;
//dùng cho các exception khi có xung đột, ví dụ: email đã tồn tại
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
