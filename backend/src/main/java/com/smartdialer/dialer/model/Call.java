package com.smartdialer.dialer.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "calls")
public class Call {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long campaignId;
    private Long borrowerId;
    private Long agentId;
    
    private String provider;
    private String providerCallId;

    @Enumerated(EnumType.STRING)
    private CallState state;

    private String failureReason;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }
    public Long getBorrowerId() { return borrowerId; }
    public void setBorrowerId(Long borrowerId) { this.borrowerId = borrowerId; }
    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getProviderCallId() { return providerCallId; }
    public void setProviderCallId(String providerCallId) { this.providerCallId = providerCallId; }
    public CallState getState() { return state; }
    public void setState(CallState state) { this.state = state; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
