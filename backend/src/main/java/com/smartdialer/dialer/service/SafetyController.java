package com.smartdialer.dialer.service;

import com.smartdialer.dialer.model.PacingDecision;
import com.smartdialer.dialer.model.SafetyDecision;
import com.smartdialer.dialer.provider.ProviderA;
import com.smartdialer.dialer.provider.ProviderHealth;
import com.smartdialer.dialer.repository.SafetyDecisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class SafetyController {

    private final SafetyDecisionRepository safetyDecisionRepository;
    private final AgentService agentService;
    private final ProviderA providerA;
    
    // Configurable thresholds
    private static final int MAX_OVERDIAL_RATIO = 2; // Never dial more than 2x available agents

    public SafetyController(SafetyDecisionRepository safetyDecisionRepository, AgentService agentService, ProviderA providerA) {
        this.safetyDecisionRepository = safetyDecisionRepository;
        this.agentService = agentService;
        this.providerA = providerA;
    }

    @Transactional
    public SafetyDecision validatePacingRequest(PacingDecision pacingRequest) {
        SafetyDecision decision = new SafetyDecision();
        decision.setCampaignId(pacingRequest.getCampaignId());
        decision.setRequestedCalls(pacingRequest.getRecommendedCalls());
        decision.setTimestamp(Instant.now());
        
        int availableAgents = (int) agentService.countAvailableAgents();
        
        if (pacingRequest.getRecommendedCalls() == 0) {
            decision.setDecision("REJECT");
            decision.setApprovedCalls(0);
            decision.setReasoning("Pacing request recommended 0 calls.");
            return safetyDecisionRepository.save(decision);
        }

        // Hard Limit Check 1: Agent Capacity
        if (availableAgents == 0) {
            decision.setDecision("REJECT");
            decision.setApprovedCalls(0);
            decision.setReasoning("REJECTED: No agents available at the time of safety check.");
            return safetyDecisionRepository.save(decision);
        }
        
        // Hard Limit Check 2: Max Over-dial limits
        int maxSafeCalls = availableAgents * MAX_OVERDIAL_RATIO;
        int requestedCalls = pacingRequest.getRecommendedCalls();

        // Check 3: Provider Health
        ProviderHealth health = providerA.getHealth();
        if (health == ProviderHealth.DOWN) {
            decision.setDecision("REJECT");
            decision.setApprovedCalls(0);
            decision.setReasoning("REJECTED: Primary provider is DOWN.");
            return safetyDecisionRepository.save(decision);
        } else if (health == ProviderHealth.DEGRADED) {
            maxSafeCalls = availableAgents; // Fallback to 1:1 progressive ratio
        }

        if (requestedCalls > maxSafeCalls) {
            decision.setDecision("REDUCE");
            decision.setApprovedCalls(maxSafeCalls);
            if (health == ProviderHealth.DEGRADED) {
                decision.setReasoning("REDUCED: Provider DEGRADED. Falling back to Progressive max: " + maxSafeCalls);
            } else {
                decision.setReasoning(String.format("REDUCED: Requested %d exceeds hard over-dial limit of %d.", 
                                                    requestedCalls, maxSafeCalls));
            }
            return safetyDecisionRepository.save(decision);
        }

        decision.setDecision("APPROVE");
        decision.setApprovedCalls(requestedCalls);
        decision.setReasoning("APPROVED: Request within safe limits. Provider Health: " + health);
        return safetyDecisionRepository.save(decision);
    }
}
