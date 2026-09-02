package com.smartstay.smartstay.dto.expenses;

public interface ExpenseSummaryProjection {
    long getTotalRecords();

    Double getTotalAmount();

    Double getTotalExpenseAmount();

    Double getTotalPaidAmount();

    Double getTotalUnPaidAmount();

    Double getTotalPartialPaidAmount();
}
