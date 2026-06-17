package com.smartstay.smartstay.dto.vendor;

import java.util.Date;

/**
 * Per-vendor roll-up of expense figures, produced by a single grouped query to avoid N+1 lookups
 * when building the vendor listing rows.
 */
public record VendorExpenseAggregate(
        String vendorId,
        Double totalPurchase,
        Double totalPaid,
        Date lastTransaction) {
}
