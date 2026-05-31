package com.org.livelocationsharing.repository;

import com.org.livelocationsharing.model.LocationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory, Long> {

    /**
     * Returns paginated location history for a session, newest first.
     * Useful for playback or audit queries.
     */
    Page<LocationHistory> findBySession_SessionIdOrderByRecordedAtDesc(
            String sessionId, Pageable pageable);
}
