package com.smartstay.smartstay.ennum;

public enum PaymentMethod {
    UPI("UPI"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    QR_CODE("QR Code");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PaymentMethod fromValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        for (PaymentMethod method : values()) {
            if (method.name().equalsIgnoreCase(trimmed) || method.value.equalsIgnoreCase(trimmed)) {
                return method;
            }
        }
        return null;
    }
}
