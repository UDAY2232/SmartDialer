package com.smartdialer.dialer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "borrowers")
public class Borrower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phoneNumber;

    private String status; // e.g., AVAILABLE, RESERVED, CALLED

    @Version
    private Long version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
