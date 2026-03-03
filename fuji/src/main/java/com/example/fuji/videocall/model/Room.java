package com.example.fuji.videocall.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents an active video-call room.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    private String roomId;

    /** Socket IDs of the two participants */
    @Builder.Default
    private Set<String> participants = ConcurrentHashMap.newKeySet();

    /** When the room was created */
    private Instant createdAt;

    /** Room state machine */
    @Builder.Default
    private RoomState state = RoomState.ACTIVE;

    /** For reconnect: which socket is currently disconnected (if any) */
    private String disconnectedSocketId;

    /** Timestamp the disconnect happened — used to enforce 10-second window */
    private Instant disconnectedAt;

    public enum RoomState {
        ACTIVE, // both peers connected
        PARTIAL, // one peer disconnected, waiting for reconnect
        CLOSED // room destroyed
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    public static Room create(String socketA, String socketB) {
        Set<String> peers = ConcurrentHashMap.newKeySet();
        peers.add(socketA);
        peers.add(socketB);
        return Room.builder()
                .roomId(UUID.randomUUID().toString())
                .participants(peers)
                .createdAt(Instant.now())
                .state(RoomState.ACTIVE)
                .build();
    }

    public String getPeerSocketId(String mySocketId) {
        return participants.stream()
                .filter(id -> !id.equals(mySocketId))
                .findFirst()
                .orElse(null);
    }

    public boolean isFull() {
        return participants.size() >= 2;
    }
}
