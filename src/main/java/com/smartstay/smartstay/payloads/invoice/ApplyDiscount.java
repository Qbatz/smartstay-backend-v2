package com.smartstay.smartstay.payloads.invoice;

import jakarta.validation.constraints.NotNull;

public record ApplyDiscount(
        @NotNull(message = "Discount amount is required")
        Double discountAmount,
        Double discountPercentage,
        String reason) {
}
