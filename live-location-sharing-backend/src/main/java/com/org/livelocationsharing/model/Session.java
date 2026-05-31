package com.org.livelocationsharing.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Represents a live location sharing session.
 * Stored in PostgreSQL as the permanent record of session lifecycle.
 */
@Entity
@Table(name = "sessions", indexes = {
        @Index(name = "idx_sessions_status", columnList = "status"),
        @Index(name = "idx_sessions_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"locationHistories"})
public class Session {

    @Id
    @Column(name = "session_id", length = 16, nullable = false, updatable = false)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private SessionStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "expired_at")
    private Instant expiredAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<LocationHistory> locationHistories;
}
