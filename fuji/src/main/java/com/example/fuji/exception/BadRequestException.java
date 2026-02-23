package com.example.fuji.exception;
//dùng cho các exception khi dữ liệu đầu vào không hợp lệ
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
