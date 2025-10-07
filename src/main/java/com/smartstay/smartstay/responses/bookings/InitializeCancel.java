package com.smartstay.smartstay.responses.bookings;

import java.util.List;

public record InitializeCancel(String bookingId,
                               String customerId,
                               Double bookingAmount,
                               String expectedJoiningDate,
                               List<CashReturnBank> listBanks) {
}
