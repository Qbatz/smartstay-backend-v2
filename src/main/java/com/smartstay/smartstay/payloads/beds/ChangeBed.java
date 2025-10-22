package com.smartstay.smartstay.payloads.beds;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ChangeBed(
        @NotNull(message = "Bed id is required")
        @Positive(message = "Bed id is required")
        Integer bedId,
        double rentAmount,
        @NotNull(message = "Joining Date is required")
        @NotEmpty(message = "Joining Date is required") String joiningDate
) {
}
