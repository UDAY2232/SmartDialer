package com.smartdialer.dialer.service;

import com.smartdialer.dialer.model.Call;
import com.smartdialer.dialer.model.CallState;
import com.smartdialer.dialer.repository.CallRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class CallService {

    private static final Logger log = LoggerFactory.getLogger(CallService.class);
    private final CallRepository callRepository;

    public CallService(CallRepository callRepository) {
        this.callRepository = callRepository;
    }

    public List<Call> getAllCalls() {
        return callRepository.findAll();
    }
    
    public Optional<Call> getCall(Long callId) {
        return callRepository.findById(callId);
    }

    @Transactional
    public Call createCall(Long campaignId, Long borrowerId, Long agentId) {
        Call call = new Call();
        call.setCampaignId(campaignId);
        call.setBorrowerId(borrowerId);
        call.setAgentId(agentId);
        call.setState(CallState.QUEUED);
        return callRepository.save(call);
    }
    
    @Transactional
    public boolean updateState(Long callId, CallState newState) {
        Call call = callRepository.findById(callId).orElseThrow(() -> new IllegalArgumentException("Call not found"));
        CallState currentState = call.getState();
        
        if (isTerminalState(currentState)) {
            log.warn("Attempt to update call {} from terminal state {} to {}. Ignoring.", callId, currentState, newState);
            return false;
        }
        
        // Prevent backward/out-of-order transitions
        if (!isValidTransition(currentState, newState)) {
            log.warn("Invalid state transition for call {}: {} -> {}. Ignoring out-of-order event.", callId, currentState, newState);
            return false;
        }
        
        call.setState(newState);
        callRepository.save(call);
        return true;
    }

    private boolean isValidTransition(CallState current, CallState next) {
        // Simple hierarchy checking for progression
        if (current == CallState.QUEUED) return next == CallState.RESERVED || next == CallState.CANCELLED;
        if (current == CallState.RESERVED) return next == CallState.INITIATED || next == CallState.CANCELLED;
        if (current == CallState.INITIATED) return next == CallState.RINGING || next == CallState.ANSWERED || next == CallState.COMPLETED || next == CallState.FAILED;
        if (current == CallState.RINGING) return next == CallState.ANSWERED || next == CallState.CONNECTED || next == CallState.COMPLETED || next == CallState.FAILED;
        if (current == CallState.ANSWERED) return next == CallState.CONNECTED || next == CallState.COMPLETED || next == CallState.FAILED;
        if (current == CallState.CONNECTED) return next == CallState.COMPLETED || next == CallState.FAILED;
        
        return false;
    }
    
    @Transactional
    public void assignProviderCallId(Long callId, String provider, String providerCallId) {
        Call call = callRepository.findById(callId).orElseThrow();
        call.setProvider(provider);
        call.setProviderCallId(providerCallId);
        callRepository.save(call);
    }
    
    private boolean isTerminalState(CallState state) {
        return state == CallState.COMPLETED || state == CallState.CANCELLED || state == CallState.FAILED;
    }
}
