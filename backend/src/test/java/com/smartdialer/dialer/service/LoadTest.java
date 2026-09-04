package com.smartdialer.dialer.service;

import com.smartdialer.dialer.model.Agent;
import com.smartdialer.dialer.model.AgentStatus;
import com.smartdialer.dialer.repository.AgentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class LoadTest {
    
    @Autowired
    private AgentRepository agentRepository;
    
    @Autowired
    private AgentService agentService;

    @Test
    public void testAgentReservationLoad() throws InterruptedException {
        int[] agentCounts = {100, 1000, 10000};
        
        for (int agentCount : agentCounts) {
            agentRepository.deleteAll(); // clear for test
            
            // Batch save for speed
            java.util.List<Agent> agentsToSave = new java.util.ArrayList<>();
            for (int i = 0; i < agentCount; i++) {
                Agent agent = new Agent();
                agent.setStatus(AgentStatus.AVAILABLE);
                agent.setVersion(0L);
                agentsToSave.add(agent);
            }
            agentRepository.saveAll(agentsToSave);

            ExecutorService executor = Executors.newFixedThreadPool(20);
            CountDownLatch latch = new CountDownLatch(agentCount);
            
            long startTime = System.currentTimeMillis();

            for (Agent agent : agentRepository.findAll()) {
                final Long agentId = agent.getId();
                executor.submit(() -> {
                    try {
                        agentService.reserveAgent(agentId);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            latch.await();
            long endTime = System.currentTimeMillis();
            System.out.println("Load test for " + agentCount + " agents took " + (endTime - startTime) + "ms");
            executor.shutdown();
        }
    }
}
