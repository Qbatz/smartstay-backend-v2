package com.smartstay.smartstay.ennum;

public enum BookingStatus {
    BOOKED("BOOKED"),
    VACATED("VACATED"),
    NOTICE("NOTICE"),
    CHECKIN("CHECKED-IN"),
    TERMINATED("TERMINATED"),
    CANCELLED("CANCELLED");


    BookingStatus(String booked) {
    }
}
