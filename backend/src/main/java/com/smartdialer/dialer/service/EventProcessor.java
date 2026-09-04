package com.smartdialer.dialer.service;

import com.smartdialer.dialer.model.*;
import com.smartdialer.dialer.repository.CallRepository;
import com.smartdialer.dialer.repository.ProviderEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;

@Service
public class EventProcessor {

    private static final Logger log = LoggerFactory.getLogger(EventProcessor.class);

    private final ProviderEventRepository eventRepository;
    private final CallRepository callRepository;
    private final AgentService agentService;
    private final CallService callService;

    public EventProcessor(ProviderEventRepository eventRepository, CallRepository callRepository, 
                          AgentService agentService, CallService callService) {
        this.eventRepository = eventRepository;
        this.callRepository = callRepository;
        this.agentService = agentService;
        this.callService = callService;
    }

    @Transactional
    public void processEvent(String providerEventId, Long callId, String eventType, Instant timestamp) {
        // Idempotency check
        Optional<ProviderEvent> existingEvent = eventRepository.findByProviderEventIdAndCallId(providerEventId, callId);
        if (existingEvent.isPresent()) {
            log.info("Duplicate event detected ({}). Ignoring.", providerEventId);
            return;
        }

        // Save event
        ProviderEvent event = new ProviderEvent();
        event.setProviderEventId(providerEventId);
        event.setCallId(callId);
        event.setEventType(eventType);
        event.setTimestamp(timestamp);
        eventRepository.save(event);

        // Fetch Call
        Call call = callRepository.findById(callId).orElse(null);
        if (call == null) {
            log.error("Received event for unknown call ID: {}", callId);
            return;
        }

        // Handle State Transition
        try {
            CallState newState = mapEventTypeToCallState(eventType);
            boolean updated = callService.updateState(callId, newState);
            
            if (updated) {
                updateAgentStateBasedOnCall(call.getAgentId(), newState, callId);
            }
            event.setProcessed(true);
            eventRepository.save(event);
        } catch (Exception e) {
            log.error("Failed to process event {} for call {}", eventType, callId, e);
        }
    }

    private CallState mapEventTypeToCallState(String eventType) {
        return switch (eventType.toUpperCase()) {
            case "INITIATED" -> CallState.INITIATED;
            case "RINGING" -> CallState.RINGING;
            case "ANSWERED" -> CallState.ANSWERED;
            case "CONNECTED" -> CallState.CONNECTED;
            case "COMPLETED" -> CallState.COMPLETED;
            case "FAILED" -> CallState.FAILED;
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }

    private void updateAgentStateBasedOnCall(Long agentId, CallState newState, Long callId) {
        switch (newState) {
            case INITIATED, RINGING -> agentService.updateStatus(agentId, AgentStatus.DIALING, callId);
            case ANSWERED, CONNECTED -> agentService.updateStatus(agentId, AgentStatus.CONNECTED, callId);
            case COMPLETED, CANCELLED, FAILED -> {
                // In a real system, might go to WRAP_UP first
                agentService.updateStatus(agentId, AgentStatus.AVAILABLE, null);
            }
            default -> { /* no-op */ }
        }
    }
}
