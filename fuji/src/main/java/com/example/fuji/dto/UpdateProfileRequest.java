package com.example.fuji.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 100, message = "Full name max 100 characters")
    private String fullName;

    @Size(max = 500, message = "Bio max 500 characters")
    private String bio;

    @Size(max = 20, message = "Phone max 20 characters")
    private String phone;

    private String gender;     // MALE | FEMALE | OTHER
    private String jlptLevel;  // N1 → N5
}