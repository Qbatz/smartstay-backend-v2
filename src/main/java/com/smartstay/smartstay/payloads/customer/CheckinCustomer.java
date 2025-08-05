package com.smartstay.smartstay.payloads.customer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CheckinCustomer(
        @NotNull(message = "Customer id required")
        @NotEmpty(message = "Customer id required")
        String customerId,
        @NotNull(message = "Floor id required")
        int floorId,
        @NotNull(message = "Room id required")
        int roomId,
        @NotNull(message = "Bed id required")
        int bedId,
        @NotNull(message = "joining date required")
        String joiningDate,
        @NotNull(message = "Advance required")
        double advanceAmount,
        @NotNull(message = "rent required")
        double rentAmount
) {
}
