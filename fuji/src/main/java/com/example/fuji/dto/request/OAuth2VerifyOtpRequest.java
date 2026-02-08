package com.example.fuji.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OAuth2VerifyOtpRequest {
    @NotBlank(message = "Session ID must not be blank")
    private String sessionId;

    @NotBlank(message = "OTP Code must not be blank")
    private String otpCode;
}
