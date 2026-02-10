package com.example.fuji.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.Data;
import com.example.fuji.enums.TransactionStatus;

@Entity
@Table(name = "transaction_logs")
@Data
public class TransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "transaction_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_transaction_logs_transaction",
            foreignKeyDefinition = "FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE"
        )
    )
    private Transaction transaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, columnDefinition = "JSON")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
