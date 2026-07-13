package com.smartstay.smartstay.responses.bookings;

import com.smartstay.smartstay.dto.booking.CustomerInfo;

public record InitializeCheckIn(Integer bedId,
                                String bedName,
                                Integer roomId,
                                Integer floorId,
                                Double bookingAmount,
                                String bookedDate,
//                                rent for a bed
                                Double rent,
                                boolean canCheckIn,
                                Integer responseCode,
                                String bookingId,
                                String expectedJoiningDate,
                                CustomerInfo customerInfo) {
}
