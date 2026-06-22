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
     * Resolves a request value to a {@link VendorPaymentStatus}. Accepts both the stored enum name
     * (e.g. {@code NO_TRANSACTION}) and the UI display name (e.g. {@code "No Transaction"}); matching
     * is case-insensitive and ignores spaces/underscores. Returns {@code null} when the value is
     * missing, blank, "ALL", or unrecognised — signalling that no status filter should be applied.
     */
    public static VendorPaymentStatus fromFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = normalize(value);
        if (normalized.isEmpty() || normalized.equals("all")) {
            return null;
        }
        for (VendorPaymentStatus status : values()) {
            if (normalize(status.name()).equals(normalized) || normalize(status.displayName).equals(normalized)) {
                return status;
            }
        }
        return null;
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase().replace("_", "").replace(" ", "");
    }
}
