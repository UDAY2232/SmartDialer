package com.smartdialer.dialer.repository;

import com.smartdialer.dialer.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    
    List<Campaign> findByStatus(String status);
    
    Optional<Campaign> findFirstByStatus(String status);
}
