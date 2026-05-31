package com.org.livelocationsharing.service;

import com.org.livelocationsharing.exception.SessionExpiredException;
import com.org.livelocationsharing.exception.SessionNotFoundException;
import com.org.livelocationsharing.model.Session;
import com.org.livelocationsharing.model.SessionStatus;
import com.org.livelocationsharing.repository.SessionRepository;
import com.org.livelocationsharing.util.SessionIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionIdGenerator sessionIdGenerator;

    /**
     * Creates a new active session and persists it to PostgreSQL.
     *
     * @return the generated sessionId (e.g. "AB12CD34")
     */
    @Transactional
    public String createSession() {
        String sessionId = sessionIdGenerator.generate();

        Session session = Session.builder()
                .sessionId(sessionId)
                .status(SessionStatus.ACTIVE)
                .build();

        sessionRepository.save(session);
        log.info("Session created: sessionId={}", sessionId);
        return sessionId;
    }

    /**
     * Validates a sessionId for an incoming location update.
     * Throws immediately if the session is missing or not ACTIVE.
     */
    @Transactional(readOnly = true)
    public void validateActiveSession(String sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    log.warn("Location update rejected — session not found: sessionId={}", sessionId);
                    return new SessionNotFoundException(sessionId);
                });

        if (session.getStatus() != SessionStatus.ACTIVE) {
            log.warn("Location update rejected — session not active: sessionId={}, status={}",
                    sessionId, session.getStatus());
            throw new SessionExpiredException(sessionId);
        }
    }

    /**
     * Marks a session EXPIRED in PostgreSQL.
     * Called by the Redis keyspace expiry listener.
     */
    @Transactional
    public void markExpired(String sessionId) {
        Instant now = Instant.now();
        int rows = sessionRepository.updateStatus(
                sessionId, SessionStatus.EXPIRED, now, now);

        if (rows > 0) {
            log.info("Session marked EXPIRED: sessionId={}", sessionId);
        } else {
            log.warn("markExpired found no session to update: sessionId={}", sessionId);
        }
    }
}
