package com.smartstay.smartstay.payloads.customer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SettlementEb(@NotNull(message = "Meter reading is required")
                           Double reading,
                           Integer roomId,
                           Integer floorId,
                           @NotNull(message = "Reading date required")
                           @NotEmpty(message = "Reading date required")
                           String readingDate) {
}
