package com.example.fuji.videocall.controller;

import com.example.fuji.videocall.service.MatchingService;
import com.example.fuji.videocall.service.RoomManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST endpoints for the Video Call module.
 * Exposes queue statistics and room health for monitoring/debugging.
 */
@RestController
@RequestMapping("/api/video-call")
@RequiredArgsConstructor
public class VideoCallRestController {

    private final MatchingService matchingService;
    private final RoomManager roomManager;

    /**
     * GET /api/video-call/status
     * Returns queue sizes and active room count for Grafana dashboards.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "queueSizes", matchingService.getQueueSizes(),
                "activeRooms", roomManager.activeRoomCount()));
    }

    /**
     * GET /api/video-call/config
     * Returns STUN/TURN server config for the frontend WebRTC PeerConnection.
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> iceConfig() {
        return ResponseEntity.ok(Map.of(
                "iceServers", java.util.List.of(
                        Map.of("urls", "stun:stun.l.google.com:19302"),
                        Map.of("urls", "stun:stun1.l.google.com:19302"),
                        Map.of("urls", "stun:stun2.l.google.com:19302")
                // Add TURN here when available:
                // Map.of("urls", "turn:your-turn-server.com", "username", "user", "credential",
                // "pass")
                )));
    }
}
