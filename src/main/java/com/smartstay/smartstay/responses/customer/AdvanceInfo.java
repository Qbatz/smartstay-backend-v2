package com.smartstay.smartstay.responses.customer;

public record AdvanceInfo(
        String invoiceDate,
        String dueDate,
        Double dueAmount,
        Double advanceAmount,
        Double bookingAmount,
        String paymentStatus,
        Double maintenanceAmount,
        Double deductionsAmount,
        Double paidAmount
) {
}
