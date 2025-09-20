package com.smartstay.smartstay.responses.bookings;

public record InitializeCheckIn(Integer bedId,
                                String bedName,
                                Double bookingAmount,
                                String bookedDate,
//                                rent for a bed
                                Double rent,
                                boolean canCheckIn,
                                String bookingId) {
}
