package com.smartstay.smartstay.payloads.beds;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddBed(
        @NotNull(message = "Bed name is required") @NotEmpty(message = "Bed name is required") String bedName,
        @NotNull
        int roomId
) {
}
