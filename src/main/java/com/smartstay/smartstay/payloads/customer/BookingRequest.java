package com.smartstay.smartstay.payloads.customer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Date;

public record BookingRequest(
        @NotNull(message = "First name is required")
        @NotEmpty(message = "First name is required") String firstName,
        String lastName,
        @NotNull(message = "Mobile no is required")
        @NotEmpty(message = "Mobile no is required") String mobile,
        String mailId, String houseNo, String street, String landmark,
        @NotNull(message = "pincode is required") int pincode,
        @NotNull(message = "city is required")
        @NotEmpty(message = "city is required") String city,
        @NotNull(message = "state is required")
        @NotEmpty(message = "state is required") String state,
        @NotNull(message = "Booking Date is required") String bookingDate,
        @NotNull(message = "Booking Amount is required")
        @Positive(message = "Booking Amount must be greater than 0") Double bookingAmount
) {}

