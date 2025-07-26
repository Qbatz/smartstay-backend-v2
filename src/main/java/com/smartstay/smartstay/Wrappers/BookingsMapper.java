package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dto.Bookings;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class BookingsMapper implements Function<Bookings, com.smartstay.smartstay.responses.bookings.Bookings> {
    @Override
    public com.smartstay.smartstay.responses.bookings.Bookings apply(Bookings bookings) {
        return new com.smartstay.smartstay.responses.bookings.Bookings(
                bookings.bookingId(),
                bookings.customerId(),
                Utils.dateToString(bookings.joiningDate()),
                bookings.rentAmount(),
                bookings.hostelId(),
                bookings.firstName(),
                bookings.city(),
                bookings.state(),
                bookings.country(),
                bookings.currentStatus(),
                bookings.emailId(),
                bookings.profilePic()
                );
    }
}
