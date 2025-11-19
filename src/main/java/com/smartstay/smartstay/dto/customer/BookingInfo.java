package com.smartstay.smartstay.dto.customer;

public record BookingInfo(String bookingDate,
                          Double bookingAmount,
                          String bookedBed,
                          String bookedFloor,
                          String bookedRoom) {
}
