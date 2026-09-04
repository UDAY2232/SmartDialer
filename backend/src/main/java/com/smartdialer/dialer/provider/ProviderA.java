package com.smartdialer.dialer.provider;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service("providerA")
public class ProviderA implements TelecomProvider {
    
    private ProviderHealth currentHealth = ProviderHealth.HEALTHY;
    
    @Override
    public CallResponse initiateCall(CallRequest request) {
        if (currentHealth == ProviderHealth.DOWN) {
            return new CallResponse(null, false, "Provider A is DOWN");
        }
        return new CallResponse("A-" + UUID.randomUUID().toString(), true, null);
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
