package com.example.fuji.dto.request;
import lombok.Data;

@Data
public class XGateCallbackRequest {
    private String transaction_id;
    private String order_id;
    private Long amount;
    private String status;
    private String signature;
}
