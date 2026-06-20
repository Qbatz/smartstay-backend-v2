package com.smartstay.smartstay.responses.vendor;

public record VendorMonthSummary(
        String month,
        long totalExpenseCount,
        long totalPaidCount,
        long totalUnpaidCount,
        long totalPartialCount,
        double totalPaidAmount,
        double totalUnpaidAmount,
        double totalPartialAmount) {
}
