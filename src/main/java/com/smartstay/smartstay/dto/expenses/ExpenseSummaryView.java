package com.smartstay.smartstay.dto.expenses;

/**
 * Projection for the expense-list summary aggregate (single grouped query), reflecting the
 * currently applied search/category filters.
 */
public interface ExpenseSummaryView {
    Double getTotalExpenseAmount();
    Double getTotalPaidAmount();
    Double getTotalUnPaidAmount();
    Double getTotalPartialPaidAmount();
}
