package com.smartdialer.dialer.service;

import com.smartdialer.dialer.model.Campaign;
import com.smartdialer.dialer.model.PacingDecision;
import com.smartdialer.dialer.repository.PacingDecisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class PacingEngine {

    private final AgentService agentService;
    private final PacingDecisionRepository pacingDecisionRepository;
    private final HistoricalMetricsService historicalMetricsService;

    public PacingEngine(AgentService agentService, PacingDecisionRepository pacingDecisionRepository, HistoricalMetricsService historicalMetricsService) {
        this.agentService = agentService;
        this.pacingDecisionRepository = pacingDecisionRepository;
        this.historicalMetricsService = historicalMetricsService;
    }

    /**
     * Recommends the number of calls to initiate.
     */
    @Transactional
    public PacingDecision calculatePacing(Campaign campaign) {
        int availableAgents = (int) agentService.countAvailableAgents();
        
        PacingDecision decision = new PacingDecision();
        decision.setCampaignId(campaign.getId());
        decision.setAvailableAgents(availableAgents);
        decision.setTimestamp(Instant.now());
        
        if (availableAgents == 0) {
            decision.setRecommendedCalls(0);
            decision.setReasoning("No available agents.");
            return pacingDecisionRepository.save(decision);
        }

        if ("PROGRESSIVE".equalsIgnoreCase(campaign.getPacingMode())) {
            // Progressive dialing: 1 call per available agent
            decision.setRecommendedCalls(availableAgents);
            decision.setReasoning("Progressive Mode: Recommended 1 call for each available agent.");
        } else {
            // Predictive dialing: Estimate required calls based on answer rate.
            double estimatedAnswerRate = historicalMetricsService.getEstimatedAnswerRate(campaign.getId());
            decision.setEstimatedAnswerRate(estimatedAnswerRate);
            
            int recommended = (int) Math.ceil(availableAgents / estimatedAnswerRate);
            decision.setRecommendedCalls(recommended);
            decision.setReasoning(String.format("Predictive Mode: %d agents / %.2f answer rate = %d calls recommended.", 
                                                availableAgents, estimatedAnswerRate, recommended));
        }

        return pacingDecisionRepository.save(decision);
    }
}
