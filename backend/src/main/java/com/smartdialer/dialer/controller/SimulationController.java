package com.smartdialer.dialer.controller;

import com.smartdialer.dialer.service.*;
import com.smartdialer.dialer.model.*;
import com.smartdialer.dialer.repository.AgentRepository;
import com.smartdialer.dialer.repository.CallRepository;
import com.smartdialer.dialer.repository.CampaignRepository;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/simulation")
@CrossOrigin(origins = "*")
public class SimulationController {

    private final PacingEngine pacingEngine;
    private final SafetyController safetyController;
    private final CallAllocator callAllocator;
    private final CampaignRepository campaignRepository;
    private final HistoricalMetricsService historicalMetricsService;
    private final AgentRepository agentRepository;
    private final CallRepository callRepository;
    private final EventProcessor eventProcessor;
    private final BorrowerService borrowerService;

    public SimulationController(PacingEngine pacingEngine, SafetyController safetyController, 
                                CallAllocator callAllocator, CampaignRepository campaignRepository,
                                HistoricalMetricsService historicalMetricsService,
                                AgentRepository agentRepository, CallRepository callRepository,
                                EventProcessor eventProcessor, BorrowerService borrowerService) {
        this.pacingEngine = pacingEngine;
        this.safetyController = safetyController;
        this.callAllocator = callAllocator;
        this.campaignRepository = campaignRepository;
        this.historicalMetricsService = historicalMetricsService;
        this.agentRepository = agentRepository;
        this.callRepository = callRepository;
        this.eventProcessor = eventProcessor;
        this.borrowerService = borrowerService;
    }
    
    @PostMapping("/scenario/{scenario}")
    public Map<String, Object> runSpecificScenario(@PathVariable String scenario) {
        if ("A".equalsIgnoreCase(scenario)) {
            historicalMetricsService.simulateChangingAnswerRate(0.20);
        } else if ("B".equalsIgnoreCase(scenario)) {
            historicalMetricsService.simulateChangingAnswerRate(0.50);
        } else if ("C".equalsIgnoreCase(scenario)) {
            historicalMetricsService.simulateChangingAnswerRate(0.70);
        }
        return runSimulationCycle();
    }

    @PostMapping("/run")
    public Map<String, Object> runSimulationCycle() {
        Campaign campaign = campaignRepository.findFirstByStatus("ACTIVE").orElse(null);
        if (campaign == null) {
            return Map.of("error", "No active campaign");
        }

        PacingDecision pacingDecision = pacingEngine.calculatePacing(campaign);
        SafetyDecision safetyDecision = safetyController.validatePacingRequest(pacingDecision);
        
        if (safetyDecision.getApprovedCalls() > 0) {
            callAllocator.allocateCalls(safetyDecision);
        }
        
        return Map.of(
            "pacingDecision", pacingDecision,
            "safetyDecision", safetyDecision
        );
    }

    /**
     * Drop N available agents by setting them to OFFLINE.
     * Simulates agents logging out / disconnecting.
     */
    @PostMapping("/drop-agents")
    public Map<String, Object> dropAgents(@RequestParam(defaultValue = "10") int count) {
        List<Agent> available = agentRepository.findByStatus(AgentStatus.AVAILABLE);
        int dropped = 0;
        for (int i = 0; i < Math.min(count, available.size()); i++) {
            Agent agent = available.get(i);
            agent.setStatus(AgentStatus.OFFLINE);
            agent.setLastStateChange(Instant.now());
            agent.setCurrentCallId(null);
            agentRepository.save(agent);
            dropped++;
        }
        long remaining = agentRepository.countByStatus(AgentStatus.AVAILABLE);
        return Map.of(
            "droppedAgents", dropped,
            "remainingAvailable", remaining,
            "status", "Dropped " + dropped + " agents. " + remaining + " still available."
        );
    }

    /**
     * Restore all OFFLINE agents back to AVAILABLE.
     */
    @PostMapping("/restore-agents")
    public Map<String, Object> restoreAgents() {
        List<Agent> offline = agentRepository.findByStatus(AgentStatus.OFFLINE);
        int restored = 0;
        for (Agent agent : offline) {
            agent.setStatus(AgentStatus.AVAILABLE);
            agent.setLastStateChange(Instant.now());
            agentRepository.save(agent);
            restored++;
        }
        long available = agentRepository.countByStatus(AgentStatus.AVAILABLE);
        return Map.of(
            "restoredAgents", restored,
            "totalAvailable", available,
            "status", "Restored " + restored + " agents. " + available + " now available."
        );
    }

    /**
     * Inject a provider event for a specific call.
     * Used to test duplicate events, out-of-order events, etc.
     */
    @PostMapping("/inject-event")
    public Map<String, Object> injectEvent(@RequestParam Long callId, @RequestParam String eventType) {
        try {
            String providerEventId = "SIM-" + UUID.randomUUID().toString();
            eventProcessor.processEvent(providerEventId, callId, eventType, Instant.now());
            
            // Fetch updated call state
            Call call = callRepository.findById(callId).orElse(null);
            String currentState = call != null ? call.getState().name() : "UNKNOWN";
            
            return Map.of(
                "status", "Event injected",
                "eventType", eventType,
                "callId", callId,
                "currentCallState", currentState,
                "providerEventId", providerEventId
            );
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    /**
     * Simulate worker crash for a specific call.
     * Sets the call's updatedAt to 30 seconds ago so RecoveryService picks it up.
     */
    @PostMapping("/crash-worker/{callId}")
    public Map<String, Object> simulateWorkerCrash(@PathVariable Long callId) {
        try {
            Call call = callRepository.findById(callId).orElseThrow();
            
            // Only crash calls in INITIATED state (that's what RecoveryService monitors)
            if (call.getState() != CallState.INITIATED) {
                // Force to INITIATED first so recovery can pick it up
                call.setState(CallState.INITIATED);
                callRepository.save(call);
            }
            
            // Set updatedAt to 30 seconds ago to trigger recovery
            Instant pastTime = Instant.now().minus(30, ChronoUnit.SECONDS);
            callRepository.updateCallTimeNative(callId, pastTime);

            return Map.of(
                "status", "Worker crash simulated for call " + callId,
                "callId", callId,
                "info", "RecoveryService will recover this call within 10 seconds"
            );
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
}
