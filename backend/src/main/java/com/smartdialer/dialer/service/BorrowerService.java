package com.smartdialer.dialer.service;

import com.smartdialer.dialer.model.Borrower;
import com.smartdialer.dialer.repository.BorrowerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BorrowerService {

    private final BorrowerRepository borrowerRepository;

    public BorrowerService(BorrowerRepository borrowerRepository) {
        this.borrowerRepository = borrowerRepository;
    }

    public List<Borrower> getAvailableBorrowers(int limit) {
        return borrowerRepository.findAvailableBorrowers(limit);
    }
    
    public Optional<Borrower> getBorrower(Long id) {
        return borrowerRepository.findById(id);
    }

    /**
     * Attempts to atomically reserve a borrower.
     * Returns true if successful, false if the borrower was already reserved by another thread/worker.
     */
    @Transactional
    public boolean reserveBorrower(Long borrowerId) {
        int updated = borrowerRepository.reserveBorrower(borrowerId);
        return updated > 0;
    }
    
    @Transactional
    public void updateStatus(Long borrowerId, String status) {
        Borrower borrower = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new IllegalArgumentException("Borrower not found"));
        borrower.setStatus(status);
        borrowerRepository.save(borrower);
    }
}
