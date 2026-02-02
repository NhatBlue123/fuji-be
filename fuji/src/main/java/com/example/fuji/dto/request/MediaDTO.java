package com.example.fuji.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MediaDTO { //Kiểu dữ liệu trả về sau khi tải lên
    private String url;
    private String publicId;
    private String resourceType;
    private Long size;
    private String format;
}
