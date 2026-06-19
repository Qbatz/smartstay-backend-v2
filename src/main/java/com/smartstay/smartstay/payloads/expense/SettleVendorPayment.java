package com.smartstay.smartstay.payloads.expense;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SettleVendorPayment(
        String paymentDate,
        String bankId,
        String paymentMethod,
        String transactionId,
        String notes,

        @NotNull(message = "At least one expense is required")
        @NotEmpty(message = "At least one expense is required")
        @Valid
        List<SettleVendorExpense> expenses) {
}
