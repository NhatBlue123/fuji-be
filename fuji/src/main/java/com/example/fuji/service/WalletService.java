package com.example.fuji.service;

import org.springframework.stereotype.Service;

import com.example.fuji.dto.response.WalletResponse;
import com.example.fuji.entity.Wallet;
import com.example.fuji.repository.WalletRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;

    public WalletResponse getMyWallet(Long userId) {

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        Long available = wallet.getBalance() - wallet.getFrozenBalance();

        WalletResponse response = new WalletResponse();
        response.setBalance(wallet.getBalance());
        response.setFrozenBalance(wallet.getFrozenBalance());
        response.setAvailableBalance(available);
        return response;
    }

    /**
     * Helper check số dư khả dụng
     */
    public void checkAvailableBalance(Wallet wallet, Long requiredAmount) {
        Long available = wallet.getBalance() - wallet.getFrozenBalance();
        if (available < requiredAmount) {
            throw new com.example.fuji.exception.BadRequestException(
                    "Số dư khả dụng không đủ. Cần " + requiredAmount + " nhưng chỉ có " + available);
        }
    }
}
