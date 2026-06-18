package com.smartstay.smartstay.dto.vendor;

import java.util.Date;

/**
 * Latest payment date per vendor, sourced from {@code expense_payments} in a single grouped query
 * to populate the "Last Transaction" column without N+1 lookups.
 */
public record VendorLastPayment(
        String vendorId,
        Date lastPaymentDate) {
}
