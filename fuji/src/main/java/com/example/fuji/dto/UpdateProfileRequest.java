package com.example.fuji.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String bio;
    private String phone;
    private String gender;
    private String jlptLevel;
    
}
