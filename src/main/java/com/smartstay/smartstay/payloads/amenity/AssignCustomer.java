package com.smartstay.smartstay.payloads.amenity;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignCustomer(
        @NotNull(message = "Customer id required")
        @NotEmpty(message = "Customer id required")
        String customerId,
        List<String> newAmenities) {
}
