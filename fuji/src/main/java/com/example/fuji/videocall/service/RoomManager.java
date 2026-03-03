package com.example.fuji.videocall.service;

import com.example.fuji.videocall.model.Room;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Manages room lifecycle:
 * - Create room pairing two participants
 * - Track socketId → roomId mapping for O(1) lookup
 * - Handle graceful 10-second reconnect window
 */
@Service
@Slf4j
public class RoomManager {

    private static final long RECONNECT_WINDOW_SECONDS = 10;

    /** roomId → Room */
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    /** socketId → roomId (O(1) room lookup by socket) */
    private final Map<String, String> socketToRoom = new ConcurrentHashMap<>();

    /** roomId → pending destroy future (cancelled on reconnect) */
    private final Map<String, ScheduledFuture<?>> destroyTimers = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, r -> {
        Thread t = new Thread(r, "room-cleanup");
        t.setDaemon(true);
        return t;
    });

    public RoomManager(MeterRegistry registry) {
        Gauge.builder("video_active_rooms", rooms, Map::size)
                .description("Number of active video call rooms")
                .register(registry);
    }

    // ─── Create ──────────────────────────────────────────────────────────────

    /**
     * Create a new room and register both participants.
     */
    public Room createRoom(String socketA, String socketB) {
        Room room = Room.create(socketA, socketB);
        rooms.put(room.getRoomId(), room);
        socketToRoom.put(socketA, room.getRoomId());
        socketToRoom.put(socketB, room.getRoomId());
        log.info("[Room] Created {} ← {} ↔ {}", room.getRoomId(), socketA, socketB);
        return room;
    }

    // ─── Lookup ──────────────────────────────────────────────────────────────

    public Optional<Room> findByRoomId(String roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }

    public Optional<Room> findBySocketId(String socketId) {
        return Optional.ofNullable(socketToRoom.get(socketId))
                .flatMap(roomId -> Optional.ofNullable(rooms.get(roomId)));
    }

    // ─── Disconnect / Reconnect ───────────────────────────────────────────────

    /**
     * Called when a participant's socket disconnects unexpectedly.
     * Holds the room for RECONNECT_WINDOW_SECONDS before destroying it.
     *
     * @param onExpire callback invoked if reconnect window expires
     *                 — receives roomId; caller should notify the remaining peer
     */
    public void onParticipantDisconnected(String socketId, Consumer<String> onExpire) {
        String roomId = socketToRoom.get(socketId);
        if (roomId == null)
            return;

        Room room = rooms.get(roomId);
        if (room == null)
            return;

        // Mark room as PARTIAL — one peer is gone
        room.setState(Room.RoomState.PARTIAL);
        room.setDisconnectedSocketId(socketId);
        room.setDisconnectedAt(Instant.now());
        log.info("[Room] {} entered PARTIAL state (disconnected: {})", roomId, socketId);

        // Schedule room destruction after reconnect window
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            Room r = rooms.get(roomId);
            if (r != null && r.getState() == Room.RoomState.PARTIAL) {
                destroyRoom(roomId);
                onExpire.accept(roomId);
                log.info("[Room] {} expired — reconnect window elapsed", roomId);
            }
        }, RECONNECT_WINDOW_SECONDS, TimeUnit.SECONDS);

        destroyTimers.put(roomId, future);
    }

    /**
     * Called when a participant successfully reconnects within the window.
     * Cancels the destroy timer and restores ACTIVE state.
     *
     * @param newSocketId the new socket assigned after reconnect
     * @return true if reconnect succeeded, false if room already expired
     */
    public boolean onParticipantReconnected(String roomId, String oldSocketId, String newSocketId) {
        Room room = rooms.get(roomId);
        if (room == null || room.getState() != Room.RoomState.PARTIAL)
            return false;

        // Cancel destroy timer
        ScheduledFuture<?> future = destroyTimers.remove(roomId);
        if (future != null)
            future.cancel(false);

        // Swap socket reference
        room.getParticipants().remove(oldSocketId);
        room.getParticipants().add(newSocketId);
        socketToRoom.remove(oldSocketId);
        socketToRoom.put(newSocketId, roomId);

        room.setState(Room.RoomState.ACTIVE);
        room.setDisconnectedSocketId(null);
        room.setDisconnectedAt(null);
        log.info("[Room] {} reconnected: {} → {}", roomId, oldSocketId, newSocketId);
        return true;
    }

    // ─── Destroy ─────────────────────────────────────────────────────────────

    /**
     * Permanently destroy a room and clean up all mappings.
     */
    public void destroyRoom(String roomId) {
        Room room = rooms.remove(roomId);
        if (room == null)
            return;

        room.getParticipants().forEach(socketToRoom::remove);
        room.setState(Room.RoomState.CLOSED);

        ScheduledFuture<?> timer = destroyTimers.remove(roomId);
        if (timer != null)
            timer.cancel(false);

        log.info("[Room] Destroyed {}", roomId);
    }

    /**
     * Remove socket from its room when the user intentionally leaves.
     * If both participants are gone, destroy the room.
     */
    public void onParticipantLeft(String socketId) {
        String roomId = socketToRoom.remove(socketId);
        if (roomId == null)
            return;
        Room room = rooms.get(roomId);
        if (room == null)
            return;
        room.getParticipants().remove(socketId);
        if (room.getParticipants().isEmpty())
            destroyRoom(roomId);
    }

    public int activeRoomCount() {
        return rooms.size();
    }
}
