package com.org.livelocationsharing.controller;

import com.org.livelocationsharing.dto.LocationResponse;
import com.org.livelocationsharing.dto.LocationUpdateRequest;
import com.org.livelocationsharing.model.LocationSnapshot;
import com.org.livelocationsharing.service.LocationService;
import com.org.livelocationsharing.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles location update (host) and location retrieval (viewer) via REST.
 * Viewers can also subscribe to WebSocket topic for real-time push.
 */
@Slf4j
@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class LocationController {

    private final SessionService sessionService;
    private final LocationService locationService;

    /**
     * POST /session/{sessionId}/location
     * Called by the host device every few seconds to push GPS coordinates.
     *
     * Flow:
     *  1. Validate session is ACTIVE
     *  2. Update Redis (sliding TTL)
     *  3. Broadcast to WebSocket subscribers
     *  4. Async-log to PostgreSQL
     *
     * @param sessionId path variable — the session to update
     * @param request   body containing latitude and longitude
     * @return 200 OK with the stored snapshot
     */
    @PostMapping("/{sessionId}/location")
    public ResponseEntity<LocationResponse> updateLocation(
            @PathVariable String sessionId,
            @Valid @RequestBody LocationUpdateRequest request) {

        log.info("Location update request: sessionId={}, lat={}, lng={}",
                sessionId, request.getLatitude(), request.getLongitude());

        sessionService.validateActiveSession(sessionId);
        LocationSnapshot snapshot = locationService.updateLocation(sessionId, request);

        return ResponseEntity.ok(toResponse(snapshot));
    }

    /**
     * GET /session/{sessionId}/location
     * Polling fallback for viewers who can't use WebSocket.
     * Reads the latest snapshot directly from Redis.
     *
     * @param sessionId path variable
     * @return 200 OK with latest location, or 404 if no data yet
     */
    @GetMapping("/{sessionId}/location")
    public ResponseEntity<LocationResponse> getLatestLocation(
            @PathVariable String sessionId) {

        log.info("Location fetch request: sessionId={}", sessionId);

        sessionService.validateActiveSession(sessionId);
        LocationSnapshot snapshot = locationService.getLatestLocation(sessionId);

        log.info("Location fetched: sessionId={}, lastUpdated={}", sessionId, snapshot.getLastUpdated());
        return ResponseEntity.ok(toResponse(snapshot));
    }

    // ── Mapper ──────────────────────────────────────────────────────────────

    private LocationResponse toResponse(LocationSnapshot snapshot) {
        return LocationResponse.builder()
                .latitude(snapshot.getLatitude())
                .longitude(snapshot.getLongitude())
                .lastUpdated(snapshot.getLastUpdated())
                .build();
    }
}

