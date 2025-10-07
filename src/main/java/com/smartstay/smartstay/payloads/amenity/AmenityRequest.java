package com.smartstay.smartstay.payloads.amenity;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AmenityRequest(
        @NotNull(message = "Amenity Name required")
        @NotEmpty(message = "Amenity Name required")
        String amenityName,

        @NotNull(message = "Amount required")
        Double amount,

        Boolean proRate


) {
}
