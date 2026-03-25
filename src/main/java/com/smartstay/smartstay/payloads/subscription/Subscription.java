package com.smartstay.smartstay.payloads.subscription;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record Subscription(
        @NotNull(message = "Plan code is required")
        @NotEmpty(message = "Plan code is required")
        String planCode,
        Double discountAmount,
        Double discountPercentage) {


}
