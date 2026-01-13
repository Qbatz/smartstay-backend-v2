package com.smartstay.smartstay.ennum;

public enum HostelEventModule {

    INVOICE("Invoice"),
    CUSTOMER("Customer"),
    ELECTRICITY("Electricity"),
    FLOOR("Floor"),
    ROOM("Room"),
    BED("Bed"),
    AMENITY("Amenity"),
    ASSET("Asset"),
    BANK("Bank"),
    BOOKING("Booking"),
    COMPLAINT("Complaint"),
    COMPLAINT_TYPE("Complaint Type"),
    CUSTOMER_CONFIG("Customer Config"),
    HOSTEL("Hostel"),
    USER("User"),
    EXPENSE("Expense"),
    EXPENSE_CATEGORY("Expense Category"),
    ROLE("Role");

    private final String displayName;

    HostelEventModule(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Safe lookup from DB or API */
    public static HostelEventModule from(String value) {
        if (value == null)
            return null;
        for (HostelEventModule module : values()) {
            if (module.name().equalsIgnoreCase(value)) {
                return module;
            }
        }
        throw new IllegalArgumentException("Invalid HostelEventModule: " + value);
    }
}
