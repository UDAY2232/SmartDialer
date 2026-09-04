package com.smartdialer.dialer.repository;

import com.smartdialer.dialer.model.Agent;
import com.smartdialer.dialer.model.AgentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {

    List<Agent> findByStatus(AgentStatus status);

    long countByStatus(AgentStatus status);

    /**
     * Atomic reservation: Only updates if the agent is still AVAILABLE.
     */
    @Modifying
    @Query("UPDATE Agent a SET a.status = 'RESERVED', a.lastStateChange = :now, a.version = a.version + 1 WHERE a.id = :id AND a.status = 'AVAILABLE'")
    int reserveAgent(@Param("id") Long id, @Param("now") java.time.Instant now);
}
