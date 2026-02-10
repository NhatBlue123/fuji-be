package com.example.fuji.dto.request;

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
public class CardDTO {

    @NotBlank(message = "Từ vựng không được để trống")
    @Size(max = 200, message = "Từ vựng không được vượt quá 200 ký tự")
    private String vocabulary;

    @NotBlank(message = "Nghĩa không được để trống")
    private String meaning;

    @Size(max = 200, message = "Cách đọc không được vượt quá 200 ký tự")
    private String pronunciation;

    private String exampleSentence;
}
