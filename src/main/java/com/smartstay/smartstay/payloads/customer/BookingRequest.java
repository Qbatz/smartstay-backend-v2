package com.smartstay.smartstay.payloads.customer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Date;

public record BookingRequest(
        @NotNull(message = "Joining date is required")
        @NotEmpty(message = "Joining date is required")
        String joiningDate,
        @NotNull(message = "Booking Date is required") String bookingDate,
        @NotNull(message = "Booking Amount is required")
        @Positive(message = "Booking Amount must be greater than 0") Double bookingAmount,
        @NotNull(message = "Floor id required")
        int floorId,
        @NotNull(message = "Room id required")
        int roomId,
        @NotNull(message = "Bed id required")
        int bedId,
        @NotNull(message = "Customer id required")
        @NotEmpty(message = "Customer id required")
        String customerId,
        @NotNull(message = "Bank id required")
        @NotEmpty(message = "Bank id required")
        String bankId,
        String referenceNumber
) {}

