package com.smartstay.smartstay.responses.vendor;

public record VendorSummary(
        long totalVendors,
        double totalPurchase,
        double totalPaid,
        double outstandingAmount) {
}
