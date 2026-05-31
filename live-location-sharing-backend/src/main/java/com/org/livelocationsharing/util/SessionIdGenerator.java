package com.org.livelocationsharing.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Generates short, human-shareable session IDs.
 * Format: 8 uppercase alphanumeric characters — e.g. "AB12CD34"
 * Derived from a UUID so collision probability is negligible.
 */
@Component
public class SessionIdGenerator {

    private static final int SESSION_ID_LENGTH = 8;

    public String generate() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, SESSION_ID_LENGTH)
                .toUpperCase();
    }
}
