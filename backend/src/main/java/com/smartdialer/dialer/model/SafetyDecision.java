package com.smartdialer.dialer.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "safety_decisions")
public class SafetyDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long campaignId;

    private int requestedCalls;
    private int approvedCalls;
    
    private String decision; // APPROVE, REDUCE, REJECT, FALLBACK
    
    @Column(columnDefinition="TEXT")
    private String reasoning;
    
    private Instant timestamp;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }
    public int getRequestedCalls() { return requestedCalls; }
    public void setRequestedCalls(int requestedCalls) { this.requestedCalls = requestedCalls; }
    public int getApprovedCalls() { return approvedCalls; }
    public void setApprovedCalls(int approvedCalls) { this.approvedCalls = approvedCalls; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
