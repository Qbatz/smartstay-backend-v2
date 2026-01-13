package com.smartstay.smartstay.ennum;

public enum HostelEventType {

    INVOICE_GENERATED("Invoice Generated"),

    FLOOR_CREATED("Floor Created"),
    INVOICE_EDITED("Invoice Edited"),
    MANUAL_INVOICE_CREATED("Manual Invoice Created"),
    EDIT_RECURRING_INVOICE("Recurring Invoice Updated"),

    ADDED_CUSTOMER("Customer Added"),
    CHECKED_IN_CUSTOMER("Customer Checked In"),
    BOOKED_CUSTOMER("Customer Booked"),
    MOVED_TO_NOTICE_PERIOD("Moved to Notice Period"),
    CHECKOUT_CUSTOMER("Customer Checked Out"),

    ADDED_ELECTRICITY("Electricity Charge Added"),

    FLOOR_UPDATED("Floor Updated"),
    FLOOR_DELETED("Floor Deleted"),

    ROOM_CREATED("Room Created"),
    ROOM_UPDATED("Room Updated"),
    ROOM_DELETED("Room Deleted"),

    BED_CREATED("Bed Created"),
    BED_UPDATED("Bed Updated"),
    BED_DELETED("Bed Deleted");

    private final String displayName;

    HostelEventType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Safe lookup from DB or API */
    public static HostelEventType from(String value) {
        if (value == null)
            return null;
        for (HostelEventType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid HostelEventType: " + value);
    }
}
