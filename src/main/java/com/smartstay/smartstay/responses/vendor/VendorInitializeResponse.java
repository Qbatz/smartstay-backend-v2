package com.smartstay.smartstay.responses.vendor;

/**
 * Minimal vendor summary returned by the expense-initialize API. Exposes only the fields needed to
 * pick a vendor and show its running balance; the financial figures are read straight from the
 * denormalized columns on the vendor row (no per-vendor aggregation).
 */
public record VendorInitializeResponse(
        int id,
        String businessName,
        String paymentStatus,
        double totalExpenseAmount,
        double totalPaidAmount,
        double totalBalance) {
}
