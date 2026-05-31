package com.org.livelocationsharing.controller;

import com.org.livelocationsharing.dto.CreateSessionResponse;
import com.org.livelocationsharing.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles session lifecycle operations.
 * Currently exposes session creation only.
 */
@Slf4j
@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    /**
     * POST /session/create
     * Creates a new live location sharing session.
     *
     * @return 201 Created with { "sessionId": "AB12CD34" }
     */
    @PostMapping("/create")
    public ResponseEntity<CreateSessionResponse> createSession() {
        log.info("Create session request received");
        String sessionId = sessionService.createSession();
        log.info("Session creation successful: sessionId={}", sessionId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new CreateSessionResponse(sessionId));
    }
}

