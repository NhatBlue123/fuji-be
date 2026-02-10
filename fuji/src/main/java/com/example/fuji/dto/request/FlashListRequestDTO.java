package com.example.fuji.dto.request;

import java.util.List;

import com.example.fuji.enums.JlptLevel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashListRequestDTO {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề không được vượt quá 200 ký tự")
    private String title;

    private String description;

    private JlptLevel level;

    private Boolean isPublic;

    private List<Long> flashcardIds;
}
