package com.smartstay.smartstay.payloads.customer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CheckoutNotice(
        @NotNull(message = "Request date required")
        @NotEmpty(message = "Request date required")
        String requestDate,
        @NotNull(message = "Checkout date required")
        @NotEmpty(message = "Checkout date required")
        String checkoutDate,
        @NotNull(message = "Customer id required")
        @NotEmpty(message = "Customer id required")
        String customerId,
        String reason) {
}
