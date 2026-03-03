package com.example.fuji.videocall.controller;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.example.fuji.videocall.dto.SignalingEvent;
import com.example.fuji.videocall.model.QueueNode;
import com.example.fuji.videocall.model.Room;
import com.example.fuji.videocall.service.MatchingService;
import com.example.fuji.videocall.service.RoomManager;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * WebRTC Signaling Controller — handles all Socket.IO events.
 *
 * Events handled:
 * join-queue → enqueue user, emit match-found if paired
 * leave-queue → remove from queue
 * offer → relay SDP offer to peer
 * answer → relay SDP answer to peer
 * ice-candidate → relay ICE candidate to peer
 * send-message → relay chat message to peer
 * reconnect-attempt → handle 10-second reconnect window
 * leave-room → clean up room on intentional disconnect
 *
 * On connect/disconnect, socket metadata is managed automatically.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SignalingController {

    private final SocketIOServer server;
    private final MatchingService matchingService;
    private final RoomManager roomManager;
    private final MeterRegistry meterRegistry;

    // Metrics
    private Counter matchSuccessCounter;
    private Counter reconnectCounter;
    private Counter disconnectCounter;

    @PostConstruct
    public void start() {
        matchSuccessCounter = Counter.builder("video_match_success_total")
                .description("Total successful matches").register(meterRegistry);
        reconnectCounter = Counter.builder("video_reconnect_attempts_total")
                .description("Total reconnect attempts").register(meterRegistry);
        disconnectCounter = Counter.builder("video_socket_disconnect_total")
                .description("Total socket disconnects").register(meterRegistry);

        server.addListeners(this);
        server.start();
        log.info("[SignalingController] SocketIO server started on port {}", server.getConfiguration().getPort());
    }

    @PreDestroy
    public void stop() {
        server.stop();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Connection lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    @OnConnect
    public void onConnect(SocketIOClient client) {
        log.debug("[Socket] Connected: {}", client.getSessionId());
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        String socketId = client.getSessionId().toString();
        disconnectCounter.increment();

        // 1. If user was still in queue, remove them
        matchingService.removeFromQueue(socketId);

        // 2. If user was in a room, start the reconnect window
        roomManager.findBySocketId(socketId).ifPresent(room -> {
            roomManager.onParticipantDisconnected(socketId, expiredRoomId -> {
                // Reconnect window expired → notify the other peer
                room.getParticipants().stream()
                        .filter(id -> !id.equals(socketId))
                        .forEach(peerId -> {
                            SocketIOClient peer = server.getClient(java.util.UUID.fromString(peerId));
                            if (peer != null) {
                                peer.sendEvent("peer-left",
                                        SignalingEvent.ErrorEvent.builder()
                                                .code("PEER_LEFT")
                                                .message("Your partner disconnected.")
                                                .build());
                            }
                        });
                roomManager.destroyRoom(expiredRoomId);
            });
            // Notify peer that their partner is temporarily gone
            String peerId = room.getPeerSocketId(socketId);
            if (peerId != null) {
                SocketIOClient peer = server.getClient(java.util.UUID.fromString(peerId));
                if (peer != null) {
                    peer.sendEvent("peer-disconnected",
                            SignalingEvent.ErrorEvent.builder()
                                    .code("PEER_DISCONNECTED")
                                    .message("Partner lost connection. Waiting 10s...")
                                    .build());
                }
            }
        });

        log.debug("[Socket] Disconnected: {}", socketId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Queue events
    // ─────────────────────────────────────────────────────────────────────────

    @OnEvent("join-queue")
    public void onJoinQueue(SocketIOClient client, SignalingEvent.JoinQueue payload) {
        String socketId = client.getSessionId().toString();
        log.info("[Queue] join-queue: socket={} level={}", socketId, payload.getJlptLevel());

        QueueNode node = QueueNode.builder()
                .socketId(socketId)
                .userId(payload.getUserId())
                .displayName(payload.getDisplayName())
                .avatarUrl(payload.getAvatarUrl())
                .jlptLevel(payload.getJlptLevel())
                .build();

        QueueNode partner = matchingService.joinQueue(node);

        if (partner != null) {
            // Create room
            Room room = roomManager.createRoom(socketId, partner.getSocketId());
            matchSuccessCounter.increment();

            // Emit to initiator (will send offer)
            client.sendEvent("match-found", SignalingEvent.MatchFound.builder()
                    .roomId(room.getRoomId())
                    .peerId(partner.getSocketId())
                    .peerName(partner.getDisplayName())
                    .peerAvatarUrl(partner.getAvatarUrl())
                    .peerLevel(partner.getJlptLevel())
                    .isInitiator(true)
                    .build());

            // Emit to partner (will wait for offer)
            SocketIOClient partnerClient = server.getClient(java.util.UUID.fromString(partner.getSocketId()));
            if (partnerClient != null) {
                partnerClient.sendEvent("match-found", SignalingEvent.MatchFound.builder()
                        .roomId(room.getRoomId())
                        .peerId(socketId)
                        .peerName(node.getDisplayName())
                        .peerAvatarUrl(node.getAvatarUrl())
                        .peerLevel(node.getJlptLevel())
                        .isInitiator(false)
                        .build());
            }
        }
        // else: user is queued — no event sent yet, client shows searching animation
    }

    @OnEvent("leave-queue")
    public void onLeaveQueue(SocketIOClient client) {
        matchingService.removeFromQueue(client.getSessionId().toString());
        log.info("[Queue] leave-queue: {}", client.getSessionId());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WebRTC signaling relay (offer / answer / ice-candidate)
    // ─────────────────────────────────────────────────────────────────────────

    @OnEvent("offer")
    public void onOffer(SocketIOClient client, SignalingEvent.Offer payload) {
        relayToPeer(client, payload.getRoomId(), "offer", payload);
    }

    @OnEvent("answer")
    public void onAnswer(SocketIOClient client, SignalingEvent.Answer payload) {
        relayToPeer(client, payload.getRoomId(), "answer", payload);
    }

    @OnEvent("ice-candidate")
    public void onIceCandidate(SocketIOClient client, SignalingEvent.IceCandidate payload) {
        relayToPeer(client, payload.getRoomId(), "ice-candidate", payload);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Chat relay
    // ─────────────────────────────────────────────────────────────────────────

    @OnEvent("send-message")
    public void onChatMessage(SocketIOClient client, SignalingEvent.ChatMessage payload) {
        payload.setTimestamp(System.currentTimeMillis());
        relayToPeer(client, payload.getRoomId(), "receive-message",
                SignalingEvent.ChatReceived.builder()
                        .senderId(payload.getSenderId())
                        .senderName(payload.getSenderName())
                        .message(payload.getMessage())
                        .timestamp(payload.getTimestamp())
                        .build());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Reconnect
    // ─────────────────────────────────────────────────────────────────────────

    @OnEvent("reconnect-attempt")
    public void onReconnectAttempt(SocketIOClient client, SignalingEvent.ReconnectAttempt payload) {
        reconnectCounter.increment();
        String newSocketId = client.getSessionId().toString();
        log.info("[Reconnect] attempt: roomId={} userId={} newSocket={}",
                payload.getRoomId(), payload.getUserId(), newSocketId);

        Optional<Room> roomOpt = roomManager.findByRoomId(payload.getRoomId());
        if (roomOpt.isEmpty() || roomOpt.get().getState() == Room.RoomState.CLOSED) {
            client.sendEvent("reconnect-result",
                    SignalingEvent.ReconnectResult.builder()
                            .success(false).roomId(payload.getRoomId())
                            .message("Room expired. Please find a new partner.")
                            .build());
            return;
        }

        Room room = roomOpt.get();
        String oldSocketId = room.getDisconnectedSocketId();
        boolean ok = roomManager.onParticipantReconnected(payload.getRoomId(), oldSocketId, newSocketId);

        if (ok) {
            client.sendEvent("reconnect-result",
                    SignalingEvent.ReconnectResult.builder()
                            .success(true).roomId(payload.getRoomId())
                            .message("Reconnected successfully. Re-negotiating...")
                            .build());

            // Notify peer to re-initiate offer
            String peerId = room.getPeerSocketId(newSocketId);
            if (peerId != null) {
                SocketIOClient peer = server.getClient(java.util.UUID.fromString(peerId));
                if (peer != null)
                    peer.sendEvent("peer-reconnected", payload.getRoomId());
            }
        } else {
            client.sendEvent("reconnect-result",
                    SignalingEvent.ReconnectResult.builder()
                            .success(false).roomId(payload.getRoomId())
                            .message("Reconnect failed.")
                            .build());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Leave room
    // ─────────────────────────────────────────────────────────────────────────

    @OnEvent("leave-room")
    public void onLeaveRoom(SocketIOClient client, SignalingEvent.LeaveRoom payload) {
        String socketId = client.getSessionId().toString();
        relayToPeer(client, payload.getRoomId(), "peer-left",
                SignalingEvent.ErrorEvent.builder()
                        .code("PEER_LEFT").message("Your partner ended the call.").build());
        roomManager.onParticipantLeft(socketId);
        log.info("[Room] leave-room: socket={} room={}", socketId, payload.getRoomId());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Relay an event to the peer in the same room.
     */
    private void relayToPeer(SocketIOClient sender, String roomId, String event, Object data) {
        roomManager.findByRoomId(roomId).ifPresent(room -> {
            String peerSocketId = room.getPeerSocketId(sender.getSessionId().toString());
            if (peerSocketId == null)
                return;
            SocketIOClient peer = server.getClient(java.util.UUID.fromString(peerSocketId));
            if (peer != null)
                peer.sendEvent(event, data);
        });
    }
}
