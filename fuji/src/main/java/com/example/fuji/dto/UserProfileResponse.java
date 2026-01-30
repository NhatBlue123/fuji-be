package com.example.fuji.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private LocalDateTime createdAt;
}
