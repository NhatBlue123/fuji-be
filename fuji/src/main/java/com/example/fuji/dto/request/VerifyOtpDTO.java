package com.example.fuji.dto.request;

import lombok.Data;


@Data
public class VerifyOtpDTO {
    private String email;
    private String otpCode;
}
