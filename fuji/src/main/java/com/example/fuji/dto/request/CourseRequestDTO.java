package com.example.fuji.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequestDTO {

    @NotBlank(message = "Tiêu đề khóa học không được để trống")
    @Size(max = 200, message = "Tiêu đề không được vượt quá 200 ký tự")
    private String title;

    @NotBlank(message = "Mô tả khóa học không được để trống")
    private String description;

    @NotNull(message = "ID giảng viên không được để trống")
    private Long instructorId;

    @NotNull(message = "Giá khóa học không được để trống")
    @DecimalMin(value = "0.0", message = "Giá khóa học phải lớn hơn hoặc bằng 0")
    private BigDecimal price;

    private Boolean isPublished;
}
