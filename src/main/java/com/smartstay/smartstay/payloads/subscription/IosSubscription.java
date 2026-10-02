package com.smartstay.smartstay.payloads.subscription;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record IosSubscription(
        @NotNull(message = "Renewal date is required")
        @NotEmpty(message = "Renewal date is required")
        String renewalDate,

        @NotNull(message = "Transaction date is required")
        @NotEmpty(message = "Transaction date is required")
        String transactionDate,

        @NotNull(message = "Status is required")
        @NotEmpty(message = "Status is required")
        String status) {
}
