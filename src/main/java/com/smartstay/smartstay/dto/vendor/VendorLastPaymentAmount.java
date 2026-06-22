package com.smartstay.smartstay.dto.vendor;

/**
 * Amount of the most recent payment per vendor, sourced from {@code expense_payments} in a single
 * query so the mobile vendor list can show "Last Transaction" as an amount without N+1 lookups.
 */
public record VendorLastPaymentAmount(
        String vendorId,
        Double amount) {
}
