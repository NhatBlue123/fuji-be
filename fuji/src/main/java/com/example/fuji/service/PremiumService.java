package com.example.fuji.service;

import com.example.fuji.entity.TransactionPayment;
import com.example.fuji.entity.User;
import com.example.fuji.entity.Wallet;
import com.example.fuji.exception.BadRequestException;
import com.example.fuji.exception.ResourceNotFoundException;
import com.example.fuji.repository.TransactionPaymentRepository;
import com.example.fuji.repository.UserRepository;
import com.example.fuji.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PremiumService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionPaymentRepository transactionPaymentRepository;
    private final WalletService walletService;

    // e.g. 200,000 VND / month
    private static final Long PREMIUM_PRICE = 200000L;

    public void subscribePremium(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Wallet not found. Please top up first."));

        walletService.checkAvailableBalance(wallet, PREMIUM_PRICE);

        Long balanceBefore = wallet.getBalance();
        Long balanceAfter = balanceBefore - PREMIUM_PRICE;

        // deduct money
        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        // Update premium expiry
        LocalDateTime currentExpiry = user.getPremiumExpireAt();
        if (currentExpiry == null || currentExpiry.isBefore(LocalDateTime.now())) {
            user.setPremiumExpireAt(LocalDateTime.now().plusDays(30));
        } else {
            user.setPremiumExpireAt(currentExpiry.plusDays(30));
        }
        userRepository.save(user);

        // Log transaction
        TransactionPayment tp = new TransactionPayment();
        tp.setUser(user);
        tp.setType("PREMIUM_SUBSCRIPTION");
        tp.setAmount(-PREMIUM_PRICE);
        tp.setBalanceBefore(balanceBefore);
        tp.setBalanceAfter(balanceAfter);
        tp.setReferenceId("PREM_" + System.currentTimeMillis());
        tp.setDescription("Đăng ký thành viên VIP (30 ngày)");
        transactionPaymentRepository.save(tp);

        log.info("User {} subscribed to Premium until {}", userId, user.getPremiumExpireAt());
    }
}
