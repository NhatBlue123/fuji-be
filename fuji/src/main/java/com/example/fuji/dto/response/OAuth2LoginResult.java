package com.example.fuji.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2LoginResult {
    private String sessionId;
    private AuthResponse authResponse;
    private boolean needsOtp;
    private String email;
}
