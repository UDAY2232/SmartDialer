package com.smartdialer.dialer.service;

import com.smartdialer.dialer.model.PacingDecision;
import com.smartdialer.dialer.model.SafetyDecision;
import com.smartdialer.dialer.provider.ProviderA;
import com.smartdialer.dialer.provider.ProviderHealth;
import com.smartdialer.dialer.repository.SafetyDecisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

public class SafetyControllerTest {

    private SafetyController safetyController;
    private SafetyDecisionRepository safetyDecisionRepository;
    private AgentService agentService;
    private ProviderA providerA;

    @BeforeEach
    public void setup() {
        safetyDecisionRepository = Mockito.mock(SafetyDecisionRepository.class);
        agentService = Mockito.mock(AgentService.class);
        providerA = Mockito.mock(ProviderA.class);
        safetyController = new SafetyController(safetyDecisionRepository, agentService, providerA);
        
        Mockito.when(safetyDecisionRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
    }

    @Test
    public void testRejectWhenProviderDown() {
        PacingDecision pacing = new PacingDecision();
        pacing.setRecommendedCalls(20);
        Mockito.when(agentService.countAvailableAgents()).thenReturn(10L);
        Mockito.when(providerA.getHealth()).thenReturn(ProviderHealth.DOWN);

        SafetyDecision result = safetyController.validatePacingRequest(pacing);
        assertEquals("REJECT", result.getDecision());
        assertEquals(0, result.getApprovedCalls());
    }

    @Test
    public void testReduceWhenOverdialLimitExceeded() {
        PacingDecision pacing = new PacingDecision();
        pacing.setRecommendedCalls(50); // Requesting 50 calls
        Mockito.when(agentService.countAvailableAgents()).thenReturn(10L); // 10 agents, max 20 calls
        Mockito.when(providerA.getHealth()).thenReturn(ProviderHealth.HEALTHY);

        SafetyDecision result = safetyController.validatePacingRequest(pacing);
        assertEquals("REDUCE", result.getDecision());
        assertEquals(20, result.getApprovedCalls()); // Max is 2x agents = 20
    }
    
    @Test
    public void testFallbackWhenProviderDegraded() {
        PacingDecision pacing = new PacingDecision();
        pacing.setRecommendedCalls(20); // Requesting 20 calls
        Mockito.when(agentService.countAvailableAgents()).thenReturn(10L); // 10 agents
        Mockito.when(providerA.getHealth()).thenReturn(ProviderHealth.DEGRADED);

        SafetyDecision result = safetyController.validatePacingRequest(pacing);
        assertEquals("REDUCE", result.getDecision());
        assertEquals(10, result.getApprovedCalls()); // Fallback to 1x agents = 10
    }
}
