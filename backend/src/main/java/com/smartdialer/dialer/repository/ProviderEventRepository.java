package com.smartdialer.dialer.repository;

import com.smartdialer.dialer.model.ProviderEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderEventRepository extends JpaRepository<ProviderEvent, Long> {
    
    Optional<ProviderEvent> findByProviderEventIdAndCallId(String providerEventId, Long callId);
}
