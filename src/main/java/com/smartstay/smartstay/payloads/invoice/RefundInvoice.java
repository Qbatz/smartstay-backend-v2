package com.smartstay.smartstay.payloads.invoice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RefundInvoice(
        @NotNull(message = "Bank id is required")
        @NotEmpty(message = "Bank id is required")
        String bankId,
        String refundDate,
        @Positive(message = "Invalid refund amount")
        Double refundAmount,
        String referenceNumber) {
}
