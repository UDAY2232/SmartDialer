package com.smartdialer.dialer.config;

import com.smartdialer.dialer.model.*;
import com.smartdialer.dialer.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Instant;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(AgentRepository agentRepository, 
                                   BorrowerRepository borrowerRepository,
                                   CampaignRepository campaignRepository) {
        return args -> {
            if (campaignRepository.count() == 0) {
                Campaign campaign = new Campaign();
                campaign.setName("Default Predictive Campaign");
                campaign.setPacingMode("PREDICTIVE");
                campaign.setStatus("ACTIVE");
                campaign.setCreatedAt(Instant.now());
                campaignRepository.save(campaign);
            }

            if (agentRepository.count() == 0) {
                for (int i = 1; i <= 20; i++) {
                    Agent agent = new Agent();
                    agent.setName("Agent " + i);
                    agent.setStatus(AgentStatus.AVAILABLE);
                    agent.setSessionStartTime(Instant.now());
                    agent.setLastStateChange(Instant.now());
                    agent.setVersion(0L);
                    agentRepository.save(agent);
                }
            }

            if (borrowerRepository.count() == 0) {
                for (int i = 1; i <= 100; i++) {
                    Borrower borrower = new Borrower();
                    borrower.setName("Borrower " + i);
                    borrower.setPhoneNumber("555-01" + String.format("%02d", i));
                    borrower.setStatus("AVAILABLE");
                    borrower.setVersion(0L);
                    borrowerRepository.save(borrower);
                }
            }
        };
    }
}
