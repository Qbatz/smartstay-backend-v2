package com.smartstay.smartstay.ennum;

public enum CustomerStatus {
    ACTIVE("active"),
    INACTIVE("inactive"),
    VACATED("vacated"),
    NOTICE("notice"),
    BOOKED("Booked"),
    CHECK_IN("Checked in"),
    WALKED_IN("walk in"),
    CANCELLED_BOOKING("Cancelled"),
    DELETED("DELETED"),
    SETTLEMENT_GENERATED("Settlement Generated");
    CustomerStatus(String active) {
    }
}
