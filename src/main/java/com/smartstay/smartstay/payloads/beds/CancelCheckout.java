package com.smartstay.smartstay.payloads.beds;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CancelCheckout(

        @NotNull(message = "Bed id is required")
        @Positive(message = "Bed id is required")
        Integer bedId,
        @NotNull(message = "Re-CheckIn Date is required")
        @NotEmpty(message = "Re-CheckIn Date is required") String reCheckInDate,
        String reason

) {
}
