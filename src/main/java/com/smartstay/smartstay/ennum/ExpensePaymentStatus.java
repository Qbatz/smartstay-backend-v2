package com.smartstay.smartstay.ennum;

public enum ExpensePaymentStatus {
    Full,
    Partial,
    Pending,
    Overdue;

    /**
     * Resolves the given value to an {@link ExpensePaymentStatus}, defaulting to {@link #Full}
     * when the value is missing, blank or not a recognised status.
     */
    public static ExpensePaymentStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return Full;
        }
        for (ExpensePaymentStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        return Full;
    }
}
