package com.example.fuji.dto.request;

import java.util.List;

import com.example.fuji.enums.JlptLevel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashCardRequestDTO {

    @NotBlank(message = "Tên bộ thẻ không được để trống")
    @Size(max = 200, message = "Tên bộ thẻ không được vượt quá 200 ký tự")
    private String name;

    private String description;

    private JlptLevel level;

    private Boolean isPublic;

    @NotEmpty(message = "Bộ thẻ phải có ít nhất 1 thẻ")
    @Valid
    private List<CardDTO> cards;
}
