package com.smartstay.smartstay.payloads.transactions;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddPayment(
        @NotEmpty(message = "Mode of transaction required")
        @NotNull(message = "Mode of transaction required")
        String modeOfTransaction,
        String paymentDate,
        @NotNull(message = "Amount required")
        Double amount) {
}
