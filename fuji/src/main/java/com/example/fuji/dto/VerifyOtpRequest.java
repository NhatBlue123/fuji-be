package com.example.fuji.dto;

import lombok.Data;


@Data
public class VerifyOtpRequest {
    private String email;
    private String otpCode;
}
