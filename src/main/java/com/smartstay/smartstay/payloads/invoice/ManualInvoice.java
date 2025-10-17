package com.smartstay.smartstay.payloads.invoice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ManualInvoice(
        @Positive(message = "Rent amount required")
        @NotNull(message = "Rent amount required")
        Double rentAmount,
        Double ebAmount,
        Double amenityAmount,
        String invoiceNumber,
        @NotNull(message = "Invoice date required")
        @NotEmpty(message = "Invoice date required")
        String invoiceDate,
        String dueDate) {
}
