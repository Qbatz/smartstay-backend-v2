package com.smartstay.smartstay.dto.vendor;

/**
 * Aggregate purchase/paid totals across all vendors matching the current search and filter
 * criteria. Used to build the {@code vendorSummary} block of the listing response.
 */
public record VendorPurchaseSummary(
        Double totalPurchase,
        Double totalPaid) {
}
