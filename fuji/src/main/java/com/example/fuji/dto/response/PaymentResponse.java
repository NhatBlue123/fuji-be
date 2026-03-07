package com.example.fuji.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private String orderId;
    private Long amount;
    private String currency;
    private String status;
    private String gateway;
    private String gatewayTransactionId;
    private Long userId;
    private String userEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
