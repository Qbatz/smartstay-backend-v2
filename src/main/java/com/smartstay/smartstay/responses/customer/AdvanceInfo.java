package com.smartstay.smartstay.responses.customer;

public record AdvanceInfo(
        String invoiceDate,
        String dueDate,
        Double dueAmount,
        Double advanceAmount,
        String paymentStatus,
        Double maintenanceAmount,
        Double deductionsAmount,
        Double paidAmount
) {
}
