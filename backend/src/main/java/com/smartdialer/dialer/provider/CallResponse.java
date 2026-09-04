package com.smartdialer.dialer.provider;

public class CallResponse {
    private String providerCallId;
    private boolean success;
    private String errorMessage;

    public CallResponse(String providerCallId, boolean success, String errorMessage) {
        this.providerCallId = providerCallId;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public String getProviderCallId() { return providerCallId; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
}
