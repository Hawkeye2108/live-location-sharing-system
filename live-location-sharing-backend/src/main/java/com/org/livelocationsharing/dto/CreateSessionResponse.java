package com.org.livelocationsharing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Response body for POST /session/create
 */
@Getter
@AllArgsConstructor
public class CreateSessionResponse {
    private final String sessionId;
}

