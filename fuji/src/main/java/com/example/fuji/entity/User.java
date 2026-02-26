package com.example.fuji.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import com.example.fuji.enums.Role;
import com.example.fuji.enums.Gender;
import com.example.fuji.enums.JlptLevel;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email", unique = true),
        @Index(name = "idx_username", columnList = "username", unique = true),
        @Index(name = "idx_is_active", columnList = "is_active"),
        @Index(name = "idx_role", columnList = "role")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

     // USERNAME có thể null nếu login bằng Google (Google user không cần username)
    @Column(unique = true)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "google_id", unique = true, length = 100)
    private String googleId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    //google OAuth (Mỗi user Google có ID riêng → tránh trùng email)
    @Column(name = "google_id", unique = true)
    private String googleId;

    //theo dõi trạng thái account (OAuth user không cần kích hoạt email)
    @Column(name = "is_active")
    private Boolean isActive = true;

    //theo dõi login (Lưu lần đăng nhập cuối cùng)
    private LocalDateTime lastLogin;

    // ===== Audit =====

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== Lifecycle =====

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) isActive = true;

    }
}