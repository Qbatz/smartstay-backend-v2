package com.smartstay.smartstay.payloads.booking;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CancelBooking(String reason, String cancelDate,
                            @NotNull(message = "Bank Id required")
                            @NotEmpty(message = "Bank Id required")
                            String bankId,
                            String referenceNumber) {
}
