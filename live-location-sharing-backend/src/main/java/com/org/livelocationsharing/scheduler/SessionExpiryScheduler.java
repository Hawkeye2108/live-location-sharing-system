package com.org.livelocationsharing.scheduler;

import com.org.livelocationsharing.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Listens for Redis keyspace expiry events.
 *
 * When Redis evicts a key matching "session:location:{sessionId}" because
 * its TTL elapsed (host stopped sending updates for 20 minutes), this
 * handler fires and marks the session EXPIRED in PostgreSQL.
 *
 * Redis must have keyspace notifications enabled:
 *   redis.conf:  notify-keyspace-events Ex
 *   or at runtime:  CONFIG SET notify-keyspace-events Ex
 *
 * The RedisConfig registers this bean as a MessageListenerAdapter
 * on the pattern __keyevent@*__:expired
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionExpiryScheduler {

    private static final String LOCATION_KEY_PREFIX = "session:location:";

    private final SessionService sessionService;

    /**
     * Called by the Redis message listener container whenever any key expires.
     *
     * @param expiredKey the full Redis key that just expired
     */
    public void onKeyExpired(String expiredKey) {
        log.debug("Redis key expired event received: key={}", expiredKey);

        if (!expiredKey.startsWith(LOCATION_KEY_PREFIX)) {
            // Not a location key — ignore
            return;
        }

        String sessionId = expiredKey.substring(LOCATION_KEY_PREFIX.length());
        log.info("Location key expired — marking session EXPIRED: sessionId={}", sessionId);

        try {
            sessionService.markExpired(sessionId);
        } catch (Exception ex) {
            log.error("Failed to mark session as expired: sessionId={}, error={}",
                    sessionId, ex.getMessage(), ex);
        }
    }
}