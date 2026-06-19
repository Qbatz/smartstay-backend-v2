package com.smartstay.smartstay.payloads.expense;

import jakarta.validation.constraints.NotNull;

public record SettleExpenseItem(
        @NotNull(message = "Expense item id is required")
        Long id,

        Double paidAmount) {
}
