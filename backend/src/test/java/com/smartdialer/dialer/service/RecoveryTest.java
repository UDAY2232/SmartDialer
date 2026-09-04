package com.smartdialer.dialer.service;

import com.smartdialer.dialer.model.Agent;
import com.smartdialer.dialer.model.AgentStatus;
import com.smartdialer.dialer.repository.AgentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class RecoveryTest {

    @Autowired
    private RecoveryService recoveryService;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private com.smartdialer.dialer.repository.BorrowerRepository borrowerRepository;

    @Autowired
    private com.smartdialer.dialer.repository.CallRepository callRepository;

    @Test
    public void testStaleAgentRecovery() {
        Agent agent = new Agent();
        agent.setName("Stale Agent");
        agent.setStatus(AgentStatus.RESERVED);
        agent.setVersion(0L);
        // Set last state change to 1 hour ago
        agent.setLastStateChange(Instant.now().minusSeconds(3600));
        agent = agentRepository.save(agent);

        recoveryService.recoverStaleAgents();
        
        Agent updatedAgent = agentRepository.findById(agent.getId()).get();
        assertEquals(AgentStatus.AVAILABLE, updatedAgent.getStatus(), "Stale agent should be recovered to AVAILABLE");
    }
    @Test
    public void testWorkerCrashRecovery() {
        // Setup Agent
        Agent agent = new Agent();
        agent.setName("Crashed Agent");
        agent.setStatus(AgentStatus.RESERVED);
        agent.setVersion(0L);
        agent = agentRepository.save(agent);

        // Setup Borrower
        com.smartdialer.dialer.model.Borrower borrower = new com.smartdialer.dialer.model.Borrower();
        borrower.setStatus("RESERVED");
        borrower.setVersion(0L);
        borrower = borrowerRepository.save(borrower);

        // Setup Call
        com.smartdialer.dialer.model.Call call = new com.smartdialer.dialer.model.Call();
        call.setState(com.smartdialer.dialer.model.CallState.INITIATED);
        call.setAgentId(agent.getId());
        call.setBorrowerId(borrower.getId());
        call = callRepository.save(call);
        
        // Force update time directly in DB to bypass JPA @PreUpdate
        callRepository.updateCallTimeNative(call.getId(), Instant.now().minusSeconds(3600));

        recoveryService.recoverStaleCalls();
        
        com.smartdialer.dialer.model.Call updatedCall = callRepository.findById(call.getId()).get();
        assertEquals(com.smartdialer.dialer.model.CallState.FAILED, updatedCall.getState(), "Stale INITIATED call should fail");

        Agent updatedAgent = agentRepository.findById(agent.getId()).get();
        assertEquals(AgentStatus.AVAILABLE, updatedAgent.getStatus(), "Agent should be freed");

        com.smartdialer.dialer.model.Borrower updatedBorrower = borrowerRepository.findById(borrower.getId()).get();
        assertEquals("AVAILABLE", updatedBorrower.getStatus(), "Borrower should be freed");
    }
}
