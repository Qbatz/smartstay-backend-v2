package com.smartstay.smartstay.ennum;

public enum KycStatus {
    PENDING("Pending"),
    REQUESTED("Requested"),
    VERIFIED("Verified"),
    EXPIRED("Request expired"),
    WAITING_FOR_APPROVAL("Waiting for approval"),
    NOT_AVAILABLE("Not Available");

    private final String status;
    KycStatus(String status) {
        this.status = status;
    }

}
