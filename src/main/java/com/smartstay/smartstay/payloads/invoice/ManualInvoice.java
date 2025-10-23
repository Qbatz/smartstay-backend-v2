package com.smartstay.smartstay.payloads.invoice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ManualInvoice(
        String invoiceNumber,
        @NotNull(message = "Invoice date required")
        @NotEmpty(message = "Invoice date required")
        String invoiceDate,
        String dueDate,
        List<ItemResponse> items

) {
}
