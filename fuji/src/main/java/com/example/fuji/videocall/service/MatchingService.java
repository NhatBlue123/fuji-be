package com.example.fuji.videocall.service;

import com.example.fuji.videocall.model.QueueNode;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * O(1) Matching Service using Doubly Linked Lists per JLPT level.
 *
 * Data structures:
 * levelQueues : Map<level, LevelDLL> — per-level FIFO queue
 * globalQueue : GlobalDLL — fallback any-level queue
 * nodeRefBySocket : Map<socketId, QueueNode> — O(1) lookup + removal
 *
 * All operations are O(1):
 * enqueue → append to DLL tail (both level + global)
 * matchSame → pop head of level queue
 * matchAny → pop head of global queue
 * remove → node pointer detach from both DLLs
 *
 * Thread safety: synchronized on 'this' for the critical section
 * (single JVM; replace with Redis Lua script for multi-node).
 */
@Service
@Slf4j
public class MatchingService {

    // ═══════════════════════════════════════════════════════════════════════
    // Inner DLL class
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Generic intrusive doubly-linked list using QueueNode pointers
     * for either the level-queue lane or the global-queue lane.
     */
    private enum Lane {
        LEVEL, GLOBAL
    }

    private static final class DLL {
        QueueNode head; // oldest (will be matched first)
        QueueNode tail; // newest
        int size;
        final Lane lane;

        DLL(Lane lane) {
            this.lane = lane;
        }

        // O(1) append to tail
        void append(QueueNode node) {
            if (lane == Lane.LEVEL) {
                node.prevLevel = tail;
                node.nextLevel = null;
                if (tail != null)
                    tail.nextLevel = node;
                else
                    head = node;
                tail = node;
            } else {
                node.prevGlobal = tail;
                node.nextGlobal = null;
                if (tail != null)
                    tail.nextGlobal = node;
                else
                    head = node;
                tail = node;
            }
            size++;
        }

        // O(1) detach any node
        void remove(QueueNode node) {
            if (lane == Lane.LEVEL) {
                if (node.prevLevel != null)
                    node.prevLevel.nextLevel = node.nextLevel;
                else
                    head = node.nextLevel;
                if (node.nextLevel != null)
                    node.nextLevel.prevLevel = node.prevLevel;
                else
                    tail = node.prevLevel;
                node.prevLevel = null;
                node.nextLevel = null;
            } else {
                if (node.prevGlobal != null)
                    node.prevGlobal.nextGlobal = node.nextGlobal;
                else
                    head = node.nextGlobal;
                if (node.nextGlobal != null)
                    node.nextGlobal.prevGlobal = node.prevGlobal;
                else
                    tail = node.prevGlobal;
                node.prevGlobal = null;
                node.nextGlobal = null;
            }
            size--;
        }

        // O(1) pop head
        QueueNode popHead() {
            if (head == null)
                return null;
            QueueNode node = head;
            remove(node);
            return node;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // State
    // ═══════════════════════════════════════════════════════════════════════

    private static final List<String> LEVELS = List.of("N1", "N2", "N3", "N4", "N5");

    /** level → dedicated DLL */
    private final Map<String, DLL> levelQueues = new HashMap<>();

    /** Global fallback queue */
    private final DLL globalQueue = new DLL(Lane.GLOBAL);

    /** socketId → node (for O(1) removal on disconnect) */
    private final Map<String, QueueNode> nodeRefBySocket = new ConcurrentHashMap<>();

    // ═══════════════════════════════════════════════════════════════════════
    // Metrics
    // ═══════════════════════════════════════════════════════════════════════

    public MatchingService(MeterRegistry registry) {
        // initialise level queues
        for (String level : LEVELS) {
            DLL dll = new DLL(Lane.LEVEL);
            levelQueues.put(level, dll);
            // expose Gauge metric per level
            Gauge.builder("video_queue_size", dll, d -> d.size)
                    .tag("level", level)
                    .description("Users waiting for match at this JLPT level")
                    .register(registry);
        }
        Gauge.builder("video_global_queue_size", globalQueue, d -> d.size)
                .description("Total users in global fallback queue")
                .register(registry);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Public API
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Enqueue a user. Returns matched partner if one is available, else null.
     * O(1) — one hash lookup + DLL append/pop.
     *
     * Algorithm:
     * 1. If same-level queue has someone → match immediately (don't enqueue self)
     * 2. Else if global queue has someone at different level who wants ANY → match
     * 3. Else enqueue self in both level + global queues
     */
    public synchronized QueueNode joinQueue(QueueNode node) {
        String level = node.getJlptLevel();
        node.setJoinedAt(System.currentTimeMillis());

        // 1. Try same-level match first
        DLL levelDll = levelQueues.get(level);
        if (levelDll != null && levelDll.head != null) {
            QueueNode partner = levelDll.popHead();
            // also remove partner from global queue
            globalQueue.remove(partner);
            nodeRefBySocket.remove(partner.getSocketId());
            log.info("[Matching] Same-level match: {} ↔ {} (level={})",
                    node.getSocketId(), partner.getSocketId(), level);
            return partner;
        }

        // 2. Try global fallback (any level)
        if (globalQueue.head != null) {
            QueueNode partner = globalQueue.popHead();
            // remove partner from its level queue too
            DLL partnerLevelDll = levelQueues.get(partner.getJlptLevel());
            if (partnerLevelDll != null)
                partnerLevelDll.remove(partner);
            nodeRefBySocket.remove(partner.getSocketId());
            log.info("[Matching] Cross-level match: {} ({}) ↔ {} ({})",
                    node.getSocketId(), level,
                    partner.getSocketId(), partner.getJlptLevel());
            return partner;
        }

        // 3. No partner → enqueue self
        if (levelDll != null)
            levelDll.append(node);
        globalQueue.append(node);
        nodeRefBySocket.put(node.getSocketId(), node);
        log.info("[Matching] Queued: {} (level={})", node.getSocketId(), level);
        return null;
    }

    /**
     * Remove user from all queues (on cancel or disconnect). O(1).
     */
    public synchronized void removeFromQueue(String socketId) {
        QueueNode node = nodeRefBySocket.remove(socketId);
        if (node == null)
            return;
        DLL levelDll = levelQueues.get(node.getJlptLevel());
        if (levelDll != null)
            levelDll.remove(node);
        globalQueue.remove(node);
        log.info("[Matching] Removed from queue: {}", socketId);
    }

    /** Is this socket currently in the queue? */
    public boolean isInQueue(String socketId) {
        return nodeRefBySocket.containsKey(socketId);
    }

    /** Current queue sizes (for health/debug endpoints) */
    public Map<String, Integer> getQueueSizes() {
        Map<String, Integer> sizes = new LinkedHashMap<>();
        for (String level : LEVELS) {
            sizes.put(level, levelQueues.get(level).size);
        }
        sizes.put("global", globalQueue.size);
        return sizes;
    }
}
