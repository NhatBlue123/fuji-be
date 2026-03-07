package com.example.fuji.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.fuji.entity.Transaction;
import com.example.fuji.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiece {
     private final TransactionRepository transactionRepository;

    public Page<Transaction> getMyTransactions(Long userId, Pageable pageable) {
        return transactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
}
