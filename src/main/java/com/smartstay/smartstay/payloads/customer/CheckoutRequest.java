package com.smartstay.smartstay.payloads.customer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        @NotNull(message = "Customer id required")
        @NotEmpty(message = "Customer id required")
        String customerId,
        @NotEmpty(message = "Relieving date required")
        @NotNull(message = "Relieving date required")
        String expectedLeavingDate,
        String relievingReason) {
}
