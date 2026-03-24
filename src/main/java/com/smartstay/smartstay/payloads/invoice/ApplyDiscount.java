package com.smartstay.smartstay.payloads.invoice;

import jakarta.validation.constraints.NotNull;

public record ApplyDiscount(
        Double discountAmount,
        Double discountPercentage,
        String reason) {
}
