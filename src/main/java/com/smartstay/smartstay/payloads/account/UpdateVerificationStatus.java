package com.smartstay.smartstay.payloads.account;

public class UpdateVerificationStatus {
    private boolean status = false;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
