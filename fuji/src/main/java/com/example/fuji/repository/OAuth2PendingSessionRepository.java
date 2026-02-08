package com.example.fuji.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.fuji.entity.OAuth2PendingSession;

import java.util.Optional;

@Repository
public interface OAuth2PendingSessionRepository extends JpaRepository<OAuth2PendingSession, String> {
    Optional<OAuth2PendingSession> findBySessionId(String sessionId);
}
