package com.smartdialer.dialer.provider;

public interface TelecomProvider {
    CallResponse initiateCall(CallRequest request);
    void cancelCall(String providerCallId);
    ProviderHealth getHealth();
}
