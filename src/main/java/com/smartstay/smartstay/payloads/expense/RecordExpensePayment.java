package com.smartstay.smartstay.payloads.expense;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RecordExpensePayment(
        @NotNull(message = "Expense id is required")
        @NotBlank(message = "Expense id is required")
        String expenseId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than 0")
        Double amount,

        String paymentDate,
        String bankId,
        String paymentMethod,
        String transactionId,
        String notes) {
}
