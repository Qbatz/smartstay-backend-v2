package com.smartstay.smartstay.ennum;

public enum KycStatus {
    PENDING("Pending"),
    REQUESTED("Requested"),
    VERIFIED("Verified");

    private final String status;
    KycStatus(String status) {
        this.status = status;
    }

}
