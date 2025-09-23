package com.smartstay.smartstay.payloads.electricity;

import jakarta.validation.constraints.NotNull;

public record AddReading(
        @NotNull(message = "Meter reading is required")
        Double reading,
        Integer roomId,
        Integer floorId,
        String readingDate) {
}
