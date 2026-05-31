package com.org.livelocationsharing.model;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Snapshot of the latest location for a session.
 * Stored in Redis as a JSON value under key: session:location:{sessionId}
 * Serializable so Jackson can round-trip it through Redis.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class LocationSnapshot implements Serializable {

    private String sessionId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Instant lastUpdated;
}
