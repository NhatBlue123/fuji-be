package com.example.fuji.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_payments", indexes = {
        @Index(name = "idx_tp_user", columnList = "user_id"),
        @Index(name = "idx_tp_reference", columnList = "reference_id")
})
@Data
public class TransactionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user thực hiện giao dịch
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // TOPUP, PAYMENT, BOOKING, REFUND, WITHDRAW
    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false)
    private Long amount;

    @Column(name = "balance_before", nullable = false)
    private Long balanceBefore;

    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;

    // liên kết tới payment hoặc booking
    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(length = 255)
    private String description;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}