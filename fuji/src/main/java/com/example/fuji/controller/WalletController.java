package com.example.fuji.controller;

import com.example.fuji.dto.response.WalletResponse;
import com.example.fuji.entity.Transaction;
import com.example.fuji.security.UserPrincipal;
import com.example.fuji.service.TransactionServiece;
import com.example.fuji.service.WalletService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final TransactionServiece transactionServiece;

    @GetMapping("/me")
    public WalletResponse getMyWallet(
            @AuthenticationPrincipal UserPrincipal principal) {

        return walletService.getMyWallet(principal.getId());
    }

    @GetMapping("/transactions")
    public Page<Transaction> getMyTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {

        return transactionServiece.getMyTransactions(principal.getId(), pageable);
    }
}