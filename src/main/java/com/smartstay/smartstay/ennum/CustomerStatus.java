package com.smartstay.smartstay.ennum;

public enum CustomerStatus {
    ACTIVE("active"),
    INACTIVE("inactive"),
    VACATED("vacated"),
    ON_NOTICE("notice"),
    BOOKED("Booked"),
    CHECK_IN("Checked in"),
    WALKED_IN("walk in");
    CustomerStatus(String active) {
    }
}
