package com.org.livelocationsharing.service;

import com.org.livelocationsharing.dto.LocationUpdateRequest;
import com.org.livelocationsharing.exception.LocationNotFoundException;
import com.org.livelocationsharing.model.LocationHistory;
import com.org.livelocationsharing.model.LocationSnapshot;
import com.org.livelocationsharing.model.Session;
import com.org.livelocationsharing.repository.LocationHistoryRepository;
import com.org.livelocationsharing.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Core service orchestrating a location update:
 *  1. Write latest snapshot to Redis (sliding TTL)
 *  2. Push snapshot to all WebSocket subscribers via STOMP broker
 *  3. Persist a history row to PostgreSQL (async, non-blocking)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private static final String REDIS_KEY_PREFIX = "session:location:";
    private static final String WS_TOPIC_PREFIX  = "/topic/session/";

    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final LocationHistoryRepository historyRepository;
    private final SessionRepository sessionRepository;

    @Value("${app.session.ttl-minutes:20}")
    private long sessionTtlMinutes;

    /**
     * Handles an incoming location update from the host.
     * Steps 1 and 2 are synchronous and fast (Redis + in-memory broker).
     * Step 3 is async (database write on a background thread).
     */
    public LocationSnapshot updateLocation(String sessionId, LocationUpdateRequest request) {
        Instant now = Instant.now();

        LocationSnapshot snapshot = LocationSnapshot.builder()
                .sessionId(sessionId)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .lastUpdated(now)
                .build();

        // ── Step 1: Write to Redis with sliding TTL ────────────────────────
        String redisKey = REDIS_KEY_PREFIX + sessionId;
        redisTemplate.opsForValue().set(
                redisKey,
                snapshot,
                Duration.ofMinutes(sessionTtlMinutes)
        );
        log.debug("Redis updated: key={}, lat={}, lng={}, ttl={}min",
                redisKey, snapshot.getLatitude(), snapshot.getLongitude(), sessionTtlMinutes);

        // ── Step 2: Broadcast to all WebSocket subscribers ─────────────────
        String topic = WS_TOPIC_PREFIX + sessionId;
        messagingTemplate.convertAndSend(topic, snapshot);
        log.debug("WebSocket broadcast sent: topic={}", topic);

        // ── Step 3: Async history log to PostgreSQL ────────────────────────
        persistLocationHistoryAsync(sessionId, snapshot);

        log.info("Location updated: sessionId={}, lat={}, lng={}",
                sessionId, snapshot.getLatitude(), snapshot.getLongitude());

        return snapshot;
    }

    /**
     * Returns the latest location snapshot from Redis.
     * Used by viewers polling via REST (fallback to WebSocket).
     */
    public LocationSnapshot getLatestLocation(String sessionId) {
        String redisKey = REDIS_KEY_PREFIX + sessionId;
        Object value = redisTemplate.opsForValue().get(redisKey);

        if (value == null) {
            log.warn("No location snapshot found in Redis: sessionId={}", sessionId);
            throw new LocationNotFoundException(sessionId);
        }

        LocationSnapshot snapshot = (LocationSnapshot) value;
        log.debug("Redis cache hit: sessionId={}, lastUpdated={}", sessionId, snapshot.getLastUpdated());
        return snapshot;
    }

    /**
     * Writes a location_history row asynchronously so the HTTP response
     * is never blocked by a database write.
     */
    @Async("asyncTaskExecutor")
    public void persistLocationHistoryAsync(String sessionId, LocationSnapshot snapshot) {
        try {
            Session sessionRef = sessionRepository.getReferenceById(sessionId);
            LocationHistory history = LocationHistory.builder()
                    .session(sessionRef)
                    .latitude(snapshot.getLatitude())
                    .longitude(snapshot.getLongitude())
                    .build();
            historyRepository.save(history);
            log.debug("Location history persisted: sessionId={}", sessionId);
        } catch (Exception ex) {
            log.error("Failed to persist location history: sessionId={}, error={}",
                    sessionId, ex.getMessage(), ex);
        }
    }
}
