package com.org.livelocationsharing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response body for GET /session/{sessionId}/location
 */
@Getter
@AllArgsConstructor
@Builder
public class LocationResponse {
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final Instant lastUpdated;
}
