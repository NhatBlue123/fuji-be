package com.example.fuji.dto.request;

import lombok.Data;

@Data
public class WithdrawDto {
    private Long amount;
    private String bankInfo;
}
