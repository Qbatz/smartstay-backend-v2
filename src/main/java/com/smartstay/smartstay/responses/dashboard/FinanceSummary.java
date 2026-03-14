package com.smartstay.smartstay.responses.dashboard;

public record FinanceSummary(Double totalIncome, Integer incomeTrend, Double totalExpense, Integer expenseTrend, Double netProfit) {
}
