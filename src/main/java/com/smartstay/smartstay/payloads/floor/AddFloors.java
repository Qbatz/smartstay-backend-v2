package com.smartstay.smartstay.payloads.floor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddFloors(
        @NotNull(message = "Floor name is required") @NotEmpty(message = "Floor name is required") String floorName,
        @NotNull
        String hostelId
        ) {
}
