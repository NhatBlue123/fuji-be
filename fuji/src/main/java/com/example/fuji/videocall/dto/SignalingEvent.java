package com.example.fuji.videocall.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * All event payload DTOs for the WebRTC Signaling layer.
 */
public class SignalingEvent {

    /** Client → Server: join matching queue */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class JoinQueue {
        private String userId;
        private String jlptLevel; // N1 | N2 | N3 | N4 | N5
        private String displayName;
        private String avatarUrl;
    }

    /** Server → Client: match found */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MatchFound {
        private String roomId;
        private String peerId; // socket-id of the other peer
        private String peerName;
        private String peerAvatarUrl;
        private String peerLevel;
        private boolean isInitiator; // true = should send offer
    }

    /** Client → Server / Server → Client: WebRTC offer */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Offer {
        private String roomId;
        private String sdp;
        private String type; // "offer"
    }

    /** Client → Server / Server → Client: WebRTC answer */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Answer {
        private String roomId;
        private String sdp;
        private String type; // "answer"
    }

    /** Client → Server / Server → Client: ICE candidate */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IceCandidate {
        private String roomId;
        private String candidate;
        private String sdpMid;
        private Integer sdpMLineIndex;
    }

    /** Client → Server: in-call text chat */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatMessage {
        private String roomId;
        private String senderId;
        private String senderName;
        private String message;
        private long timestamp;
    }

    /** Server → Client: a chat message from the peer */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatReceived {
        private String senderId;
        private String senderName;
        private String message;
        private long timestamp;
    }

    /** Client → Server: leave room voluntarily */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LeaveRoom {
        private String roomId;
    }

    /** Client → Server: attempting reconnect after drop */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReconnectAttempt {
        private String roomId;
        private String userId;
    }

    /** Server → Client: reconnect succeeded */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReconnectResult {
        private boolean success;
        private String roomId;
        private String message;
    }

    /** Server → Client: error event */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorEvent {
        private String code;
        private String message;
    }
}
