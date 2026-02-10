package com.example.fuji.dto.request;

import java.util.List;

import com.example.fuji.enums.JlptLevel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashCardUpdateDTO {

    @Size(max = 200, message = "Tên bộ thẻ không được vượt quá 200 ký tự")
    private String name;

    private String description;

    private JlptLevel level;

    private Boolean isPublic;

    @Valid
    private List<CardDTO> cards;
}
