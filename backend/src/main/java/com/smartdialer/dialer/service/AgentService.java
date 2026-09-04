package com.smartdialer.dialer.service;

import com.smartdialer.dialer.model.Agent;
import com.smartdialer.dialer.model.AgentStatus;
import com.smartdialer.dialer.repository.AgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class AgentService {

    private final AgentRepository agentRepository;

    public AgentService(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    public List<Agent> getAllAgents() {
        return agentRepository.findAll();
    }

    public List<Agent> getAvailableAgents() {
        return agentRepository.findByStatus(AgentStatus.AVAILABLE);
    }
    
    public long countAvailableAgents() {
        return agentRepository.countByStatus(AgentStatus.AVAILABLE);
    }

    /**
     * Attempts to atomically reserve an agent.
     * Returns true if successful, false if the agent was already reserved by another thread/worker.
     */
    @Transactional
    public boolean reserveAgent(Long agentId) {
        int updated = agentRepository.reserveAgent(agentId, Instant.now());
        return updated > 0;
    }
    
    @Transactional
    public void updateStatus(Long agentId, AgentStatus status, Long callId) {
        Agent agent = agentRepository.findById(agentId)
            .orElseThrow(() -> new IllegalArgumentException("Agent not found"));
            
        // Basic state transition logic
        agent.setStatus(status);
        agent.setLastStateChange(Instant.now());
        
        if (callId != null) {
            agent.setCurrentCallId(callId);
        } else if (status == AgentStatus.AVAILABLE || status == AgentStatus.OFFLINE) {
            agent.setCurrentCallId(null);
        }
        
        if (status == AgentStatus.AVAILABLE && agent.getSessionStartTime() == null) {
             agent.setSessionStartTime(Instant.now());
        }
        
        agentRepository.save(agent);
    }
}
