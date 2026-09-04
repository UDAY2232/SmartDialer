package com.smartdialer.dialer.service;

import com.smartdialer.dialer.model.Agent;
import com.smartdialer.dialer.model.AgentStatus;
import com.smartdialer.dialer.repository.AgentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RecoveryService {

    private static final Logger log = LoggerFactory.getLogger(RecoveryService.class);
    private final AgentRepository agentRepository;
    private final com.smartdialer.dialer.repository.CallRepository callRepository;
    private final BorrowerService borrowerService;

    public RecoveryService(AgentRepository agentRepository, 
                           com.smartdialer.dialer.repository.CallRepository callRepository,
                           BorrowerService borrowerService) {
        this.agentRepository = agentRepository;
        this.callRepository = callRepository;
        this.borrowerService = borrowerService;
    }

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void recoverStaleAgents() {
        Instant threshold = Instant.now().minus(15, ChronoUnit.SECONDS); // 15 sec for demo speed
        List<Agent> staleAgents = agentRepository.findByStatus(AgentStatus.RESERVED).stream()
                .filter(a -> a.getLastStateChange() != null && a.getLastStateChange().isBefore(threshold))
                .toList();

        for (Agent agent : staleAgents) {
            log.warn("Recovering stale agent {} - Reverting to AVAILABLE.", agent.getId());
            agent.setStatus(AgentStatus.AVAILABLE);
            agent.setLastStateChange(Instant.now());
            agentRepository.save(agent);
        }
    }

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void recoverStaleCalls() {
        Instant threshold = Instant.now().minus(15, ChronoUnit.SECONDS);
        List<com.smartdialer.dialer.model.Call> staleCalls = callRepository.findByState(com.smartdialer.dialer.model.CallState.INITIATED).stream()
                .filter(c -> c.getUpdatedAt() != null && c.getUpdatedAt().isBefore(threshold))
                .toList();

        for (com.smartdialer.dialer.model.Call call : staleCalls) {
            log.warn("Recovering stale call {} due to worker crash - Reverting to FAILED.", call.getId());
            call.setState(com.smartdialer.dialer.model.CallState.FAILED);
            call.setFailureReason("Worker crashed during INITIATED state");
            callRepository.save(call);
            
            // Release Agent
            agentRepository.findById(call.getAgentId()).ifPresent(agent -> {
                agent.setStatus(AgentStatus.AVAILABLE);
                agent.setLastStateChange(Instant.now());
                agentRepository.save(agent);
            });
            
            // Release Borrower
            if (call.getBorrowerId() != null) {
                borrowerService.updateStatus(call.getBorrowerId(), "AVAILABLE");
            }
        }
    }
}
