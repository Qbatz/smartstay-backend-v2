package com.smartstay.smartstay.payloads.transactions;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AddPayment(
        @NotNull(message = "Select bank account required")
        @NotEmpty(message = "Select bank account required")
        String bankId,
        String paymentDate,
        String referenceId,
        @NotNull(message = "Amount required")
        Double amount) {
}
