package com.smartstay.smartstay.ennum;

public enum VendorPaymentStatus {
    FULLY_SETTLED("Fully Settled"),
    PARTIALLY_PAID("Partially Paid"),
    NOT_PAID("Not Paid"),
    NO_TRANSACTION("No Transaction");

    private final String displayName;

    VendorPaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Resolves a request value to a {@link VendorPaymentStatus}. Returns {@code null} when the value
     * is missing, blank, "ALL", or unrecognised — signalling that no status filter should be applied.
     */
    public static VendorPaymentStatus fromFilter(String value) {
        if (value == null || value.isBlank() || value.trim().equalsIgnoreCase("ALL")) {
            return null;
        }
        for (VendorPaymentStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        return null;
    }
}
