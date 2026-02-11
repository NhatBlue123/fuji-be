package com.example.fuji.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "certificates", uniqueConstraints = {
    @UniqueConstraint(name = "uk_certificate_user_course", columnNames = {"user_id", "course_id"}),
    @UniqueConstraint(name = "uk_certificate_code", columnNames = {"certificate_code"})
})
@Data
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_certificates_user",
            foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
        )
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "course_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_certificates_course",
            foreignKeyDefinition = "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE"
        )
    )
    private Course course;

    @Column(name = "certificate_code", nullable = false, unique = true, length = 50)
    private String certificateCode;

    @Column(name = "certificate_url", length = 500)
    private String certificateUrl;

    @Column(name = "completion_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal completionPercentage;

    @Column(name = "final_score", precision = 5, scale = 2)
    private BigDecimal finalScore;

    @CreationTimestamp
    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;
}
