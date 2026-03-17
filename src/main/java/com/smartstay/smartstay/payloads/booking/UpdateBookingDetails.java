package com.smartstay.smartstay.payloads.booking;

public record UpdateBookingDetails(String joiningDate,
                                   String reason,
                                   Double newRent,
                                   String effectiveDate) {
}
