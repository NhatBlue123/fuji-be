package com.example.fuji.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "withdraw_requests")
@Data
public class WithdrawRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(nullable = false)
    private Long amount;

    // PENDING, SUCCESS, REJECTED
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "bank_info", columnDefinition = "TEXT", nullable = false)
    private String bankInfo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
