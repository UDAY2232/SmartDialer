package com.smartdialer.dialer.repository;

import com.smartdialer.dialer.model.PacingDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacingDecisionRepository extends JpaRepository<PacingDecision, Long> {
}
