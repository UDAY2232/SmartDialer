package com.smartdialer.dialer.provider;

public class CallRequest {
    private Long internalCallId;
    private String borrowerPhone;

    public CallRequest(Long internalCallId, String borrowerPhone) {
        this.internalCallId = internalCallId;
        this.borrowerPhone = borrowerPhone;
    }

    public Long getInternalCallId() { return internalCallId; }
    public String getBorrowerPhone() { return borrowerPhone; }
}
