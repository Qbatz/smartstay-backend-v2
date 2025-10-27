package com.smartstay.smartstay.ennum;

public enum PaymentStatus {
    PAID("Paid"),
    PENDING("Pending"),
    PARTIAL_PAYMENT("Partial Payment"),
    ADVANCE_IN_HAND("Advance in hand"),
    CANCELLED("Cancelled"),
    PENDING_REFUND("Refund"),
    REFUNDED("Refunded");


    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
