package com.smartdialer.dialer.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "campaigns")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    // PROGRESSIVE, PREDICTIVE
    private String pacingMode; 
    
    private String status; // ACTIVE, PAUSED, COMPLETED
    
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPacingMode() { return pacingMode; }
    public void setPacingMode(String pacingMode) { this.pacingMode = pacingMode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
