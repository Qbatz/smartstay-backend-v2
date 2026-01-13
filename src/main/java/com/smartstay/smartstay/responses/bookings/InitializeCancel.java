package com.smartstay.smartstay.responses.bookings;

import com.smartstay.smartstay.responses.banking.DebitsBank;

import java.util.List;

public record InitializeCancel(
        String hostelId,
        String bookingId,
                               String customerId,
                               Double bookingAmount,
                               String expectedJoiningDate,
                               String roomName,
                               String bedName,
                               String floorName,
                               List<DebitsBank> listBanks) {
}
