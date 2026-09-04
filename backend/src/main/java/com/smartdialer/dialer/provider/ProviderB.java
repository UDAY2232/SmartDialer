package com.smartdialer.dialer.provider;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.Random;

@Service("providerB")
public class ProviderB implements TelecomProvider {
    
    private final Random random = new Random();
    private ProviderHealth currentHealth = ProviderHealth.HEALTHY;
    
    @Override
    public CallResponse initiateCall(CallRequest request) {
        if (currentHealth == ProviderHealth.DOWN) {
            return new CallResponse(null, false, "Provider B is DOWN");
        }
        // Simulate higher failure rate (10%)
        if (random.nextInt(100) < 10) { 
            return new CallResponse(null, false, "Provider B random failure");
        }
        return new CallResponse("B-" + UUID.randomUUID().toString(), true, null);
    }
    
    @Override
    public void cancelCall(String providerCallId) {
        // Mock cancel
    }
    
    @Override
    public ProviderHealth getHealth() {
        return currentHealth;
    }
    
    public void setHealth(ProviderHealth health) {
        this.currentHealth = health;
    }
}
