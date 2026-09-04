package com.smartdialer.dialer.service;

import com.smartdialer.dialer.model.Call;
import com.smartdialer.dialer.model.CallState;
import com.smartdialer.dialer.repository.CallRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class IdempotencyTest {

    @Autowired
    private EventProcessor eventProcessor;

    @Autowired
    private CallRepository callRepository;
    
    @Autowired
    private com.smartdialer.dialer.repository.AgentRepository agentRepository;

    @Test
    public void testDuplicateEventHandling() {
        com.smartdialer.dialer.model.Agent agent = new com.smartdialer.dialer.model.Agent();
        agent.setStatus(com.smartdialer.dialer.model.AgentStatus.RESERVED);
        agent.setVersion(0L);
        agent = agentRepository.save(agent);

        Call call = new Call();
        call.setState(CallState.INITIATED);
        call.setAgentId(agent.getId());
        call = callRepository.save(call);

        eventProcessor.processEvent("event-ans", call.getId(), "ANSWERED", Instant.now());
        assertEquals(CallState.ANSWERED, callRepository.findById(call.getId()).get().getState());

        eventProcessor.processEvent("event-ans", call.getId(), "ANSWERED", Instant.now());
        eventProcessor.processEvent("event-ans", call.getId(), "ANSWERED", Instant.now());
        assertEquals(CallState.ANSWERED, callRepository.findById(call.getId()).get().getState(), "Duplicate events should be ignored");

        eventProcessor.processEvent("event-comp", call.getId(), "COMPLETED", Instant.now());
        assertEquals(CallState.COMPLETED, callRepository.findById(call.getId()).get().getState());
    }

    @Test
    public void testOutOfOrderEvents() {
        com.smartdialer.dialer.model.Agent agent = new com.smartdialer.dialer.model.Agent();
        agent.setStatus(com.smartdialer.dialer.model.AgentStatus.RESERVED);
        agent.setVersion(0L);
        agent = agentRepository.save(agent);

        Call call = new Call();
        call.setState(CallState.COMPLETED);
        call.setAgentId(agent.getId());
        call = callRepository.save(call);

        eventProcessor.processEvent("event-late-ans", call.getId(), "ANSWERED", Instant.now());
        assertEquals(CallState.COMPLETED, callRepository.findById(call.getId()).get().getState(), "Cannot overwrite COMPLETED with ANSWERED");

        eventProcessor.processEvent("event-late-ring", call.getId(), "RINGING", Instant.now());
        assertEquals(CallState.COMPLETED, callRepository.findById(call.getId()).get().getState(), "Cannot overwrite COMPLETED with RINGING");
    }
}
