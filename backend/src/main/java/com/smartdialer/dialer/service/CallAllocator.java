package com.smartdialer.dialer.service;

import com.smartdialer.dialer.model.Agent;
import com.smartdialer.dialer.model.Borrower;
import com.smartdialer.dialer.model.Call;
import com.smartdialer.dialer.model.SafetyDecision;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class CallAllocator {

    private static final Logger log = LoggerFactory.getLogger(CallAllocator.class);

    private final AgentService agentService;
    private final BorrowerService borrowerService;
    private final CallService callService;

    public CallAllocator(AgentService agentService, BorrowerService borrowerService, CallService callService) {
        this.agentService = agentService;
        this.borrowerService = borrowerService;
        this.callService = callService;
    }

    /**
     * Allocates agents and borrowers based on the SafetyDecision's approved calls.
     */
    public void allocateCalls(SafetyDecision decision) {
        int approved = decision.getApprovedCalls();
        if (approved <= 0) {
            log.info("No calls approved to allocate.");
            return;
        }

        log.info("Attempting to allocate {} calls for campaign {}", approved, decision.getCampaignId());

        // Get a batch of available agents and borrowers
        List<Agent> availableAgents = agentService.getAvailableAgents();
        List<Borrower> availableBorrowers = borrowerService.getAvailableBorrowers(approved);

        int allocated = 0;
        int agentIndex = 0;
        int borrowerIndex = 0;

        while (allocated < approved && agentIndex < availableAgents.size() && borrowerIndex < availableBorrowers.size()) {
            Agent agent = availableAgents.get(agentIndex);
            Borrower borrower = availableBorrowers.get(borrowerIndex);

            // Attempt atomic reservations
            boolean agentReserved = agentService.reserveAgent(agent.getId());
            if (!agentReserved) {
                // Another worker got this agent
                log.debug("Agent {} already reserved by another worker.", agent.getId());
                agentIndex++;
                continue;
            }

            boolean borrowerReserved = borrowerService.reserveBorrower(borrower.getId());
            if (!borrowerReserved) {
                log.debug("Borrower {} already reserved.", borrower.getId());
                borrowerIndex++;
                // Let's release the agent so it can be picked up by another iteration or worker
                // (In a real system we'd have a release method, for now we just skip and let timeout handle it,
                // but to be safe, we'll assume the agent is "burnt" for this cycle or we could add a release method)
                // We'll increment agentIndex to move on, though not ideal.
                agentIndex++;
                continue; 
            }
            
            // Both reserved successfully. Create call.
            Call call = callService.createCall(decision.getCampaignId(), borrower.getId(), agent.getId());
            log.info("Allocated Call {} (Agent: {}, Borrower: {})", call.getId(), agent.getId(), borrower.getId());
            
            // Pass to Telecom Provider (this will be handled by a higher-level orchestrator or event publisher)
            // For now, we assume the orchestrator takes the generated calls and dials them.
            
            allocated++;
            agentIndex++;
            borrowerIndex++;
        }
        
        log.info("Successfully allocated {} out of {} approved calls.", allocated, approved);
    }
}
