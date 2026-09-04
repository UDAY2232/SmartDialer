package com.smartdialer.dialer.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "provider_events", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"providerEventId", "callId"})
})
public class ProviderEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String providerEventId;
    private Long callId;
    private String eventType;
    private Instant timestamp;
    
    private boolean processed;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProviderEventId() { return providerEventId; }
    public void setProviderEventId(String providerEventId) { this.providerEventId = providerEventId; }
    public Long getCallId() { return callId; }
    public void setCallId(Long callId) { this.callId = callId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }
}
