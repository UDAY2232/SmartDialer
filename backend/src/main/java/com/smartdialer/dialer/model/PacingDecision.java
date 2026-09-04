package com.smartdialer.dialer.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "pacing_decisions")
public class PacingDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long campaignId;

    private int availableAgents;
    private double estimatedAnswerRate;
    
    private int recommendedCalls;
    
    @Column(columnDefinition="TEXT")
    private String reasoning;
    
    private Instant timestamp;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }
    public int getAvailableAgents() { return availableAgents; }
    public void setAvailableAgents(int availableAgents) { this.availableAgents = availableAgents; }
    public double getEstimatedAnswerRate() { return estimatedAnswerRate; }
    public void setEstimatedAnswerRate(double estimatedAnswerRate) { this.estimatedAnswerRate = estimatedAnswerRate; }
    public int getRecommendedCalls() { return recommendedCalls; }
    public void setRecommendedCalls(int recommendedCalls) { this.recommendedCalls = recommendedCalls; }
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
