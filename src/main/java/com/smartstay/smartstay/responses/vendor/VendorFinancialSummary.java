package com.smartstay.smartstay.responses.vendor;

public record VendorFinancialSummary(
        double totalExpense,
        double totalPaid,
        double outstanding,
        long expenseCount,
        long paymentsCounts) {
}
