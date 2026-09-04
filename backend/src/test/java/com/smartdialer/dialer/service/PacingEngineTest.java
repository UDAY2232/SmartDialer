package com.smartdialer.dialer.service;

import com.smartdialer.dialer.model.Campaign;
import com.smartdialer.dialer.model.PacingDecision;
import com.smartdialer.dialer.repository.PacingDecisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

public class PacingEngineTest {

    private PacingEngine pacingEngine;
    private AgentService agentService;
    private PacingDecisionRepository pacingDecisionRepository;
    private HistoricalMetricsService historicalMetricsService;

    @BeforeEach
    public void setup() {
        agentService = Mockito.mock(AgentService.class);
        pacingDecisionRepository = Mockito.mock(PacingDecisionRepository.class);
        historicalMetricsService = Mockito.mock(HistoricalMetricsService.class);
        pacingEngine = new PacingEngine(agentService, pacingDecisionRepository, historicalMetricsService);
        
        Mockito.when(pacingDecisionRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
    }

    @Test
    public void testProgressivePacing() {
        Campaign campaign = new Campaign();
        campaign.setPacingMode("PROGRESSIVE");
        Mockito.when(agentService.countAvailableAgents()).thenReturn(10L);

        PacingDecision result = pacingEngine.calculatePacing(campaign);
        assertEquals(10, result.getRecommendedCalls());
    }

    @Test
    public void testPredictivePacing() {
        Campaign campaign = new Campaign();
        campaign.setId(1L);
        campaign.setPacingMode("PREDICTIVE");
        Mockito.when(agentService.countAvailableAgents()).thenReturn(10L);
        Mockito.when(historicalMetricsService.getEstimatedAnswerRate(1L)).thenReturn(0.5); // 50% answer rate

        PacingDecision result = pacingEngine.calculatePacing(campaign);
        assertEquals(20, result.getRecommendedCalls()); // 10 / 0.5 = 20
    }
}
