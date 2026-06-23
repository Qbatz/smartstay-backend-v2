package com.smartstay.smartstay.responses.vendor;

public record VendorMonthSummary(
        String month,
        long totalExpenseCount,
        long totalFullPaidCount,
        long totalUnpaidCount,
        long totalPartialPaidCount,
        double totalFullPaidAmount,
        double totalUnpaidAmount,
        double totalPartialPaidAmount,
        double totalAmount,
        double balanceAmount,
        double paidAmount) {
}
