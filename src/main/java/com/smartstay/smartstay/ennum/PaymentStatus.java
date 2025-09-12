package com.smartstay.smartstay.ennum;

public enum PaymentStatus {
    PAID("Paid"),
    PENDING("Pending"),
    PARTIAL_PAYMENT("Partial Payment"),
    ADVANCE_IN_HAND("Advance in hand");

    PaymentStatus(String paid) {
    }
}
