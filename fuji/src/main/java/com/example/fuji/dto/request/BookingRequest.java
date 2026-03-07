package com.example.fuji.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingRequest {
    private Long teacherId;
    private Long price;
    private LocalDateTime scheduledAt;
}
