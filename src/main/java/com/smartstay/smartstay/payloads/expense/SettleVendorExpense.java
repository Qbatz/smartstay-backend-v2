package com.smartstay.smartstay.payloads.expense;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SettleVendorExpense(
        @NotNull(message = "Expense id is required")
        @NotBlank(message = "Expense id is required")
        String expenseId,

        Double paidAmount) {
}
