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
    ELECTRICITY_CREATED("Electricity Reading Created"),
    ELECTRICITY_UPDATED("Electricity Reading Updated"),
    ELECTRICITY_DELETED("Electricity Reading Deleted"),

    FLOOR_UPDATED("Floor Updated"),
    FLOOR_DELETED("Floor Deleted"),

    ROOM_CREATED("Room Created"),
    ROOM_UPDATED("Room Updated"),
    ROOM_DELETED("Room Deleted"),

    BED_CREATED("Bed Created"),
    BED_UPDATED("Bed Updated"),
    BED_DELETED("Bed Deleted"),

    AMENITY_CREATED("Amenity Created"),
    AMENITY_UPDATED("Amenity Updated"),
    AMENITY_ASSIGNED("Amenity Assigned"),
    AMENITY_ASSIGNED_TO_CUSTOMER("Amenity Assigned to Customer"),

    AMENITY_UNASSIGNED("Amenity Unassigned"),
    AMENITY_DELETED("Amenity Deleted"),

    ASSET_CREATED("Asset Created"),
    ASSET_UPDATED("Asset Updated"),
    ASSET_DELETED("Asset Deleted"),

    ASSET_ASSIGNED("Asset Assigned"),

    BANK_CREATED("Bank Created"),
    BANK_UPDATED("Bank Updated"),

    MONEY_ADDED("Money Added to Bank"),
    MONEY_TRANSFERRED("Money Transferred between Banks"),

    BANK_DELETED("Bank Deleted"),

    BOOKING_CREATED("Booking Created"),
    BOOKING_UPDATED("Booking Updated"),
    BOOKING_DELETED("Booking Deleted"),

    COMPLAINT_CREATED("Complaint Created"),
    COMPLAINT_COMMENT_CREATED("Complaint Comment Created"),
    COMPLAINT_UPDATED("Complaint Updated"),
    COMPLAINT_ASSIGNED("Complaint Assigned"),
    COMPLAINT_DELETED("Complaint Deleted"),

    COMPLAINT_TYPE_CREATED("Complaint Type Created"),
    COMPLAINT_TYPE_UPDATED("Complaint Type Updated"),
    COMPLAINT_TYPE_DELETED("Complaint Type Deleted"),

    CUSTOMER_CONFIG_CREATED("Customer Config Created"),
    CUSTOMER_CONFIG_UPDATED("Customer Config Updated"),
    CUSTOMER_CONFIG_DELETED("Customer Config Deleted"),

    CUSTOMER_CREATED("Customer Created"),
    CUSTOMER_UPDATED("Customer Updated"),
    CHECKOUT_CANCELLED("Checkout Cancelled"),
    CUSTOMER_BED_CHANGE("Customer Bed Changed"),
    CUSTOMER_ASSIGNED_TO_BED("Customer Assigned to Bed"),
    CUSTOMER_DELETED("Customer Deleted"),

    HOSTEL_CREATED("Hostel Created"),
    HOSTEL_UPDATED("Hostel Updated"),
    HOSTEL_DELETED("Hostel Deleted"),

    USER_CREATED("User Created"),
    USER_UPDATED("User Updated"),
    USER_DELETED("User Deleted"),

    INVOICE_CREATED("Invoice Created"),
    INVOICE_UPDATED("Invoice Updated"),
    INVOICE_DELETED("Invoice Deleted"),
    PAYMENT_RECORDED("Payment Recorded"),
    USER_HOSTEL_MAPPED("User Mapped to Hostel"),
    USER_HOSTEL_UNMAPPED("User Unmapped from Hostel"),

    EXPENSE_CREATED("Expense Created"),
    EXPENSE_UPDATED("Expense Updated"),
    EXPENSE_DELETED("Expense Deleted"),

    EXPENSE_CATEGORY_CREATED("Expense Category Created"),
    EXPENSE_CATEGORY_UPDATED("Expense Category Updated"),
    EXPENSE_CATEGORY_DELETED("Expense Category Deleted"),

    EXPENSE_SUB_CATEGORY_CREATED("Expense Sub-Category Created"),
    EXPENSE_SUB_CATEGORY_UPDATED("Expense Sub-Category Updated"),
    EXPENSE_SUB_CATEGORY_DELETED("Expense Sub-Category Deleted"),

    ROLE_CREATED("Role Created"),
    ROLE_UPDATED("Role Updated"),
    ROLE_DELETED("Role Deleted");

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
