package com.smartstay.smartstay.payloads.beds;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AssignBed(
        @NotNull(message = "Floor id required") int floorId,
        @NotNull(message = "Room id required")int roomId,
        @NotNull(message = "Bed id required")int bedId,
        @NotNull(message = "Hostel id required")String hostelId,
        @NotNull(message = "Joining date required")String joiningDate,
        @NotNull(message = "Customer id required")
        @NotEmpty(message = "Customer id required") String customerId,
        @NotNull(message = "Invoice date required")
        @NotEmpty(message = "Invoice date required")
        String invoiceDate,

        @NotNull(message = "Due date required")
        @NotEmpty(message = "Due date required")
        String dueDate,
        double advanceAmount, double rentalAmount) {
}
