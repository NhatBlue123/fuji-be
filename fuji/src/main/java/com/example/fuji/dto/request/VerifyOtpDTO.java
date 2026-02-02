package com.example.fuji.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class VerifyOtpDTO {
    private String email;
    @NotBlank(message="OTP Code must not be blank")
    private String otpCode;
}
