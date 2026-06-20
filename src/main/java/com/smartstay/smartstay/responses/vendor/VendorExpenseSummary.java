package com.smartstay.smartstay.responses.vendor;

/**
 * Minimal expense summary returned by the vendor settlement initialize API. {@code totalBalance} is
 * the current outstanding balance on the expense.
 */
public record VendorExpenseSummary(
        String expenseId,
        String expenseNo,
        Double totalAmount,
        Double totalBalance,
        String referenceNo,
        String paymentStatus) {
}
