package com.example.fuji.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class VerifyOtpRequest {
    @NotBlank(message="Email must not be blank")
    @Email(message="Email format is invalid")
    private String email;
    @NotBlank(message="OTP Code must not be blank")
    private String otpCode;
}
