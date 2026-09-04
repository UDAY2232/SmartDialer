package com.smartdialer.dialer.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "agents")
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private AgentStatus status;

    private Long currentCallId;

    private Instant sessionStartTime;

    private Instant lastStateChange;

    @Version
    private Long version; // Optimistic locking

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public AgentStatus getStatus() { return status; }
    public void setStatus(AgentStatus status) { this.status = status; }
    public Long getCurrentCallId() { return currentCallId; }
    public void setCurrentCallId(Long currentCallId) { this.currentCallId = currentCallId; }
    public Instant getSessionStartTime() { return sessionStartTime; }
    public void setSessionStartTime(Instant sessionStartTime) { this.sessionStartTime = sessionStartTime; }
    public Instant getLastStateChange() { return lastStateChange; }
    public void setLastStateChange(Instant lastStateChange) { this.lastStateChange = lastStateChange; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
