package com.smartstay.smartstay.dto.booking;

public record BookingTableHeader(String invoiceId,
                                 boolean canApply,
                                 double availableAmount,
                                 String customerId) {
}
