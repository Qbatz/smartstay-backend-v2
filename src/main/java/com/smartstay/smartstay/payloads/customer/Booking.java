package com.smartstay.smartstay.payloads.customer;

public record Booking(
    String joiningDateTentative,
    boolean refuseAdvanceAmount
) {
}
