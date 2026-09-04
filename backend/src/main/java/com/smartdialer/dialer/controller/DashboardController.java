package com.smartdialer.dialer.controller;

import com.smartdialer.dialer.model.AgentStatus;
import com.smartdialer.dialer.model.CallState;
import com.smartdialer.dialer.provider.ProviderA;
import com.smartdialer.dialer.provider.ProviderB;
import com.smartdialer.dialer.repository.AgentRepository;
import com.smartdialer.dialer.repository.CallRepository;
import com.smartdialer.dialer.service.HistoricalMetricsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*") // For local dev
public class DashboardController {

    private final AgentRepository agentRepository;
    private final CallRepository callRepository;
    private final HistoricalMetricsService historicalMetricsService;
    private final ProviderA providerA;
    private final ProviderB providerB;

    public DashboardController(AgentRepository agentRepository, CallRepository callRepository,
                               HistoricalMetricsService historicalMetricsService,
                               ProviderA providerA, ProviderB providerB) {
        this.agentRepository = agentRepository;
        this.callRepository = callRepository;
        this.historicalMetricsService = historicalMetricsService;
        this.providerA = providerA;
        this.providerB = providerB;
    }

    @GetMapping
    public Map<String, Object> getDashboardStats() {
        long availableAgents = agentRepository.countByStatus(AgentStatus.AVAILABLE);
        long totalAgents = agentRepository.count();

        // Active calls = INITIATED + RINGING + ANSWERED + CONNECTED
        long activeCalls = callRepository.countByStateIn(
            List.of(CallState.INITIATED, CallState.RINGING, CallState.ANSWERED, CallState.CONNECTED)
        );
        long connectedCalls = callRepository.countByState(CallState.CONNECTED);
        long completedCalls = callRepository.countByState(CallState.COMPLETED);
        long failedCalls = callRepository.countByState(CallState.FAILED);
        long totalCalls = callRepository.count();

        // Real utilization: (non-available agents) / total agents * 100
        int agentUtilization = 0;
        if (totalAgents > 0) {
            long busyAgents = totalAgents - availableAgents - agentRepository.countByStatus(AgentStatus.OFFLINE);
            agentUtilization = (int) Math.round((double) busyAgents / totalAgents * 100);
        }

        // Real answer rate from historical data
        double answerRate = historicalMetricsService.getEstimatedAnswerRate(1L) * 100;

        // Agent breakdown
        long reservedAgents = agentRepository.countByStatus(AgentStatus.RESERVED);
        long dialingAgents = agentRepository.countByStatus(AgentStatus.DIALING);
        long connectedAgents = agentRepository.countByStatus(AgentStatus.CONNECTED);
        long offlineAgents = agentRepository.countByStatus(AgentStatus.OFFLINE);

        Map<String, Object> stats = new HashMap<>();
        stats.put("availableAgents", availableAgents);
        stats.put("totalAgents", totalAgents);
        stats.put("activeCalls", activeCalls);
        stats.put("connectedCalls", connectedCalls);
        stats.put("completedCalls", completedCalls);
        stats.put("failedCalls", failedCalls);
        stats.put("totalCalls", totalCalls);
        stats.put("answerRate", Math.round(answerRate));
        stats.put("agentUtilization", agentUtilization);
        stats.put("reservedAgents", reservedAgents);
        stats.put("dialingAgents", dialingAgents);
        stats.put("connectedAgents", connectedAgents);
        stats.put("offlineAgents", offlineAgents);
        stats.put("providerAHealth", providerA.getHealth().name());
        stats.put("providerBHealth", providerB.getHealth().name());
        return stats;
    }
}
