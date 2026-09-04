package com.smartdialer.dialer.controller;

import com.smartdialer.dialer.provider.ProviderA;
import com.smartdialer.dialer.provider.ProviderB;
import com.smartdialer.dialer.provider.ProviderHealth;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/failure")
@CrossOrigin(origins = "*")
public class FailureController {

    private final ProviderA providerA;
    private final ProviderB providerB;

    public FailureController(ProviderA providerA, ProviderB providerB) {
        this.providerA = providerA;
        this.providerB = providerB;
    }

    /**
     * Get current health of all providers.
     * All browser tabs poll this to stay in sync.
     */
    @GetMapping("/provider-health")
    public Map<String, String> getProviderHealth() {
        return Map.of(
            "providerA", providerA.getHealth().name(),
            "providerB", providerB.getHealth().name()
        );
    }

    @PostMapping("/provider-outage/{provider}")
    public Map<String, String> simulateProviderOutage(@PathVariable String provider) {
        if ("A".equalsIgnoreCase(provider)) {
            providerA.setHealth(ProviderHealth.DOWN);
            return Map.of("status", "Provider A set to DOWN");
        }
        if ("B".equalsIgnoreCase(provider)) {
            providerB.setHealth(ProviderHealth.DOWN);
            return Map.of("status", "Provider B set to DOWN");
        }
        return Map.of("error", "Unknown provider");
    }
    
    @PostMapping("/provider-recover/{provider}")
    public Map<String, String> simulateProviderRecovery(@PathVariable String provider) {
        if ("A".equalsIgnoreCase(provider)) {
            providerA.setHealth(ProviderHealth.HEALTHY);
            return Map.of("status", "Provider A set to HEALTHY");
        }
        if ("B".equalsIgnoreCase(provider)) {
            providerB.setHealth(ProviderHealth.HEALTHY);
            return Map.of("status", "Provider B set to HEALTHY");
        }
        return Map.of("error", "Unknown provider");
    }

    @PostMapping("/provider-degrade/{provider}")
    public Map<String, String> simulateProviderDegraded(@PathVariable String provider) {
        if ("A".equalsIgnoreCase(provider)) {
            providerA.setHealth(ProviderHealth.DEGRADED);
            return Map.of("status", "Provider A set to DEGRADED");
        }
        if ("B".equalsIgnoreCase(provider)) {
            providerB.setHealth(ProviderHealth.DEGRADED);
            return Map.of("status", "Provider B set to DEGRADED");
        }
        return Map.of("error", "Unknown provider");
    }
}
