package com.smartdialer.dialer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HistoricalMetricsService {

    private static final Logger log = LoggerFactory.getLogger(HistoricalMetricsService.class);
    private List<HistoricalCall> historicalCalls = new ArrayList<>();

    public void setHistoricalData(List<HistoricalCall> calls) {
        this.historicalCalls = calls;
    }

    public double getEstimatedAnswerRate(Long campaignId) {
        if (historicalCalls.isEmpty()) return 0.50;
        
        long total = historicalCalls.size(); // Simplified to total dataset for now
        long answered = historicalCalls.stream()
            .filter(c -> "ANSWERED".equalsIgnoreCase(c.getOutcome()) || "CONNECTED".equalsIgnoreCase(c.getOutcome()))
            .count();
            
        return (double) answered / total;
    }

    public double getAverageTalkTime(Long campaignId) {
        if (historicalCalls.isEmpty()) return 120.0;
        
        return historicalCalls.stream()
            .filter(c -> "ANSWERED".equalsIgnoreCase(c.getOutcome()) || "CONNECTED".equalsIgnoreCase(c.getOutcome()))
            .mapToInt(HistoricalCall::getTalkTimeSeconds)
            .average()
            .orElse(120.0);
    }
    
    public void simulateChangingAnswerRate(double newRate) {
        historicalCalls.clear();
        int total = 100;
        int answered = (int) (total * newRate);
        for (int i = 0; i < total; i++) {
            HistoricalCall call = new HistoricalCall();
            call.setOutcome((i < answered) ? "ANSWERED" : "NO_ANSWER");
            historicalCalls.add(call);
        }
        log.info("Simulated Answer Rate changed to {}", newRate);
    }

    public static class HistoricalCall {
        private String callId;
        private Long campaignId;
        private String provider;
        private String outcome;
        private int talkTimeSeconds;

        public String getCallId() { return callId; }
        public void setCallId(String callId) { this.callId = callId; }

        public Long getCampaignId() { return campaignId; }
        public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }

        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }

        public String getOutcome() { return outcome; }
        public void setOutcome(String outcome) { this.outcome = outcome; }

        public int getTalkTimeSeconds() { return talkTimeSeconds; }
        public void setTalkTimeSeconds(int talkTimeSeconds) { this.talkTimeSeconds = talkTimeSeconds; }
    }
}
