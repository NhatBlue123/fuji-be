package com.example.fuji.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.Data;
import com.example.fuji.enums.MessageRole;

@Entity
@Table(name = "ai_messages", indexes = {
    @Index(name = "idx_conversation_id", columnList = "conversation_id")
})
@Data
public class AiMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "conversation_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_messages_conversation",
            foreignKeyDefinition = "FOREIGN KEY (conversation_id) REFERENCES ai_conversations(id) ON DELETE CASCADE"
        )
    )
    private AiConversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
