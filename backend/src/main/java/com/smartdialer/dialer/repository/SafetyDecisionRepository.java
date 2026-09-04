package com.smartdialer.dialer.repository;

import com.smartdialer.dialer.model.SafetyDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SafetyDecisionRepository extends JpaRepository<SafetyDecision, Long> {
}
