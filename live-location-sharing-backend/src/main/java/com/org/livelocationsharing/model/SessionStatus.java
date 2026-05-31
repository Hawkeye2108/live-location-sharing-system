package com.org.livelocationsharing.model;

/**
 * Lifecycle states of a location sharing session.
 */
public enum SessionStatus {

    /** Session is active and accepting location updates. */
    ACTIVE,

    /** Session has been expired due to host inactivity (Redis TTL elapsed). */
    EXPIRED,

    /** Session was explicitly terminated by the host. */
    TERMINATED
}

