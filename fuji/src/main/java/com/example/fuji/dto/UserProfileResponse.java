package com.example.fuji.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;

    private String fullName;
    private String avatarUrl;
    private String bio;
    private String gender;
    private String phone;

    private String jlptLevel;

    private boolean active;

    private LocalDateTime createdAt;
}
