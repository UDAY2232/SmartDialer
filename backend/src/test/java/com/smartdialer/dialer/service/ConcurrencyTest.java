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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ConcurrencyTest {

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private BorrowerService borrowerService;

    @Autowired
    private com.smartdialer.dialer.repository.BorrowerRepository borrowerRepository;

    @Test
    public void testConcurrentAgentReservation() throws InterruptedException {
        Agent agent = new Agent();
        agent.setName("Test Agent");
        agent.setStatus(AgentStatus.AVAILABLE);
        agent.setVersion(0L);
        agent = agentRepository.save(agent);
        final Long agentId = agent.getId();

        int numThreads = 500;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    boolean reserved = agentService.reserveAgent(agentId);
                    if (reserved) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        doneLatch.await();

        assertEquals(1, successCount.get(), "Only one thread should successfully reserve the agent out of 500 attempts");
        
        Agent updatedAgent = agentRepository.findById(agentId).get();
        assertEquals(AgentStatus.RESERVED, updatedAgent.getStatus());
    }

    @Test
    public void testConcurrentBorrowerReservation() throws InterruptedException {
        com.smartdialer.dialer.model.Borrower borrower = new com.smartdialer.dialer.model.Borrower();
        borrower.setName("Test Borrower");
        borrower.setPhoneNumber("1234567890");
        borrower.setStatus("AVAILABLE");
        borrower.setVersion(0L);
        borrower = borrowerRepository.save(borrower);
        final Long borrowerId = borrower.getId();

        int numThreads = 500;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    boolean reserved = borrowerService.reserveBorrower(borrowerId);
                    if (reserved) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        doneLatch.await();

        assertEquals(1, successCount.get(), "Only one thread should successfully reserve the borrower out of 500 attempts");
        
        com.smartdialer.dialer.model.Borrower updatedBorrower = borrowerRepository.findById(borrowerId).get();
        assertEquals("RESERVED", updatedBorrower.getStatus());
    }
}
