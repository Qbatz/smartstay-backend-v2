package com.smartstay.smartstay.payloads.booking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateAdvance(
        @NotNull(message = "Advance amount required")
                @Positive(message = "Invalid advance amount")
        Double advanceAmount) {
}
