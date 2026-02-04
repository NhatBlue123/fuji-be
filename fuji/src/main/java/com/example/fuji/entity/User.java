package com.example.fuji.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_email", columnList = "email", unique = true),
    @Index(name = "idx_username", columnList = "username", unique = true),
    @Index(name = "idx_is_active", columnList = "is_active")
})
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

     // USERNAME có thể null nếu login bằng Google (Google user không cần username)
    @Column(unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "full_name")
    private String fullName;

    //google OAuth (Mỗi user Google có ID riêng → tránh trùng email)
    @Column(name = "google_id", unique = true)
    private String googleId;

    //theo dõi trạng thái account (OAuth user không cần kích hoạt email)
    @Column(name = "is_active")
    private Boolean isActive = true;

    //theo dõi login (Lưu lần đăng nhập cuối cùng)
    private LocalDateTime lastLogin;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) isActive = true;

    }
}
