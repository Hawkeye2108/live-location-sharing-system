package com.org.livelocationsharing.repository;

import com.org.livelocationsharing.model.Session;
import com.org.livelocationsharing.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface SessionRepository extends JpaRepository<Session,String> {
    /**
     * Updates session status and sets expiredAt in one query — avoids a load-then-save cycle.
     */
    @Modifying
    @Query("""
            UPDATE Session s
               SET s.status    = :status,
                   s.expiredAt = :expiredAt,
                   s.updatedAt = :updatedAt
             WHERE s.sessionId = :sessionId
            """)
    int updateStatus(@Param("sessionId") String sessionId,
                     @Param("status") SessionStatus status,
                     @Param("expiredAt") Instant expiredAt,
                     @Param("updatedAt") Instant updatedAt);
}
