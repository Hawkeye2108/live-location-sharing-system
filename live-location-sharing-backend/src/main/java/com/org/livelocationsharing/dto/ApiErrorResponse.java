package com.org.livelocationsharing.dto;

import lombok.Getter;

import java.time.Instant;

/**
 * Standard error envelope returned by the GlobalExceptionHandler.
 * All 4xx and 5xx responses use this shape.
 */
@Getter
public class ApiErrorResponse {

    private final int     status;
    private final String  error;
    private final String  message;
    private final Instant timestamp;

    public ApiErrorResponse(int status, String error, String message) {
        this.status    = status;
        this.error     = error;
        this.message   = message;
        this.timestamp = Instant.now();
    }
}
