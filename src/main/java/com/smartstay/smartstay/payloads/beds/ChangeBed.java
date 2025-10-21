package com.smartstay.smartstay.payloads.beds;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ChangeBed(
        double rentAmount,
         @NotNull(message = "Joining Date is required")
        @NotEmpty(message = "Joining Date is required") String joiningDate
) {
}
