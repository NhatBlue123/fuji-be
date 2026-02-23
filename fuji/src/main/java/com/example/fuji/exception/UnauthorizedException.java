package com.example.fuji.exception;
//dùng cho các exception khi chưa đăng nhập
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
