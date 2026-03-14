package com.smartstay.smartstay.responses.dashboard;

import java.util.List;

public record ExpenseSummary(Double totalAmount, List<ExpenseBreakdown> breakdown) {
}
