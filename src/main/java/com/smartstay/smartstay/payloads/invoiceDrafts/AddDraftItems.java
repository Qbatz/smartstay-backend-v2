package com.smartstay.smartstay.payloads.invoiceDrafts;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddDraftItems(
        @NotEmpty(message = "Name is required")
        @NotNull(message = "Name is required")
        String name,
        Double amount) {
}
