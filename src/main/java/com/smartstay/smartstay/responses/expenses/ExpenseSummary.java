package com.smartstay.smartstay.responses.expenses;

public record ExpenseSummary(
        double totalExpenseAmount,
        double totalPaidAmount,
        double totalUnPaidAmount,
        double totalPartialPaidAmount) {
}
