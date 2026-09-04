package com.smartdialer.dialer.repository;

import com.smartdialer.dialer.model.Call;
import com.smartdialer.dialer.model.CallState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CallRepository extends JpaRepository<Call, Long> {
    
    List<Call> findByState(CallState state);
    
    List<Call> findByStateIn(List<CallState> states);

    Optional<Call> findByProviderCallId(String providerCallId);
    
    long countByState(CallState state);
    
    long countByStateIn(List<CallState> states);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE calls SET updated_at = :time WHERE id = :id", nativeQuery = true)
    void updateCallTimeNative(@org.springframework.data.repository.query.Param("id") Long id, @org.springframework.data.repository.query.Param("time") java.time.Instant time);
}
