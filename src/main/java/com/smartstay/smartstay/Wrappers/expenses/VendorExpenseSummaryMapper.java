package com.smartstay.smartstay.Wrappers.expenses;

import com.smartstay.smartstay.dao.ExpensesV1;
import com.smartstay.smartstay.responses.vendor.VendorExpenseSummary;

import java.util.function.Function;

/**
 * Maps an {@link ExpensesV1} entity to the lightweight {@link VendorExpenseSummary} returned by the
 * vendor settlement initialize API. Keeping the mapping here (instead of a JPQL constructor
 * projection) decouples the query from the DTO constructor, so changes to {@link VendorExpenseSummary}
 * cannot cause runtime projection failures.
 */
public class VendorExpenseSummaryMapper implements Function<ExpensesV1, VendorExpenseSummary> {

    @Override
    public VendorExpenseSummary apply(ExpensesV1 expense) {
        return new VendorExpenseSummary(
                expense.getExpenseId(),
                expense.getExpenseNumber(),
                expense.getTotalPrice(),
                expense.getBalanceAmount(),
                expense.getTransactionId(),
                expense.getPaymentStatus() != null ? expense.getPaymentStatus().name() : null);
    }
}
