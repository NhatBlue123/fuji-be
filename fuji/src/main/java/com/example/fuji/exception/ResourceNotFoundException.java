package com.example.fuji.exception;
//dùng cho các exception khi không tìm thấy tài nguyên
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
