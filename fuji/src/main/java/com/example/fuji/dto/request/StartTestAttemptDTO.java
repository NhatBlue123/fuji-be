package com.example.fuji.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartTestAttemptDTO {

    @NotNull(message = "ID đề thi không được để trống")
    private Long testId;
}
