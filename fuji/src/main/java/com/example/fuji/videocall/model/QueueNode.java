package com.example.fuji.videocall.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Represents a user node inside the O(1) matching queue.
 * Each node holds prev/next pointers for the Doubly Linked List,
 * plus metadata about which queues this user is enrolled in.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueNode {

    /** Socket ID (= node identifier in the queue) */
    private String socketId;

    /** Application-level user ID (from JWT) */
    private String userId;

    /** Display name */
    private String displayName;

    /** Avatar URL */
    private String avatarUrl;

    /** JLPT level preference: N1 | N2 | N3 | N4 | N5 */
    private String jlptLevel;

    /** Timestamp when the user joined the queue (for wait-time metrics) */
    private long joinedAt;

    // ────────────────────────────────────────────────────────
    // DLL pointers — for O(1) removal from level queue
    // ────────────────────────────────────────────────────────
    public QueueNode prevLevel;
    public QueueNode nextLevel;

    // DLL pointers — for O(1) removal from global queue
    public QueueNode prevGlobal;
    public QueueNode nextGlobal;
}
