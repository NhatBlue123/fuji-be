package com.example.fuji.dto.response;

import lombok.Data;
@Data
public class WalletResponse {
    private Long balance;
    private Long frozenBalance;
    private Long availableBalance;
}
