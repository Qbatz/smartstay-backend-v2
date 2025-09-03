package com.smartstay.smartstay.payloads.customer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record NonRefundable(
        @NotNull(message = "Type is required")
        @NotEmpty(message = "Type is required")
        String type,
        @NotNull(message = "Amount is required")
        Double amount) {
}
