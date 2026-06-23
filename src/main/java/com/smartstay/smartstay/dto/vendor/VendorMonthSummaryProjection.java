package com.smartstay.smartstay.dto.vendor;

/**
 * Projection for the month-wise vendor expense aggregate (one row per year+month with expenses),
 * produced by a single grouped query.
 */
public interface VendorMonthSummaryProjection {
    Integer getExpenseYear();
    Integer getExpenseMonth();
    Long getTotalExpenseCount();
    Long getTotalFullPaidCount();
    Long getTotalUnpaidCount();
    Long getTotalPartialPaidCount();
    Double getTotalFullPaidAmount();
    Double getTotalUnpaidAmount();
    Double getTotalPartialPaidAmount();
    Double getTotalAmount();
    Double getBalanceAmount();
    Double getPaidAmount();
}
