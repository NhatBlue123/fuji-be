package com.example.fuji.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "wallets", indexes = {
        @Index(name = "idx_wallet_user", columnList = "user_id", unique = true)
})
@Data
public class Wallet {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1 User = 1 Wallet
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Long balance = 0L;

    @Column(name = "frozen_balance", nullable = false)
    private Long frozenBalance = 0L;

    // chống update đồng thời
    @Version
    private Long version;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
