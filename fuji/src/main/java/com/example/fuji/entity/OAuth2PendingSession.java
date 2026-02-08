package com.example.fuji.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "oauth2_pending_sessions")
@Data
public class OAuth2PendingSession {
    @Id
    private String sessionId = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String email;

    private String googleId;

    private String fullName;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime createdAt = LocalDateTime.now();
}
