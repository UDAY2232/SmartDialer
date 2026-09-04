package com.smartdialer.dialer.repository;

import com.smartdialer.dialer.model.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowerRepository extends JpaRepository<Borrower, Long> {

    List<Borrower> findByStatus(String status);
    
    @Query(value = "SELECT * FROM borrowers WHERE status = 'AVAILABLE' LIMIT :limit", nativeQuery = true)
    List<Borrower> findAvailableBorrowers(@Param("limit") int limit);

    @Modifying
    @Query("UPDATE Borrower b SET b.status = 'RESERVED', b.version = b.version + 1 WHERE b.id = :id AND b.status = 'AVAILABLE'")
    int reserveBorrower(@Param("id") Long id);
}
