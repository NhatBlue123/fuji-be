package com.example.fuji.entity;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_user", columnList = "user_id"),
        @Index(name = "idx_order_id", columnList = "order_id", unique = true),
        @Index(name = "idx_gateway_txn", columnList = "gateway_transaction_id")
})
@Data
public class Payment {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ===== Liên kết user =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ===== Order của hệ thống bạn =====
    @Column(name = "order_id", nullable = false, unique = true, length = 100)
    private String orderId;

    // ===== Tiền =====
    @Column(nullable = false)
    private Long amount; // VND

    @Column(nullable = false, length = 10)
    private String currency = "VND";

    // ===== Gateway =====
    @Column(name = "gateway_transaction_id", length = 100)
    private String gatewayTransactionId;

    @Column(nullable = false, length = 20)
    private String gateway = "XGATE";

    // ===== Trạng thái =====
    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, SUCCESS, FAILED

    // ===== Audit =====
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
