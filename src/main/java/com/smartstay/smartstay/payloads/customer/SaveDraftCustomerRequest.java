package com.smartstay.smartstay.payloads.customer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * Draft save payload that can carry all fields used by:
 * - v2/customers/save
 * - v2/customers/booking
 * - v2/customers/check-in
 *
 * Note: No profilePic / image fields are included by design.
 */
public record SaveDraftCustomerRequest(
        // v2/customers/save
        @NotNull(message = "First name is required")
        @NotEmpty(message = "First name is required")
        String firstName,
        String lastName,
        @NotNull(message = "Mobile number required")
        @NotEmpty(message = "Mobile number required")
        String mobile,
        String emailId,

        // booking/check-in shared
        String joiningDate,

        // v2/customers/booking
        String bookingDate,
        @Positive(message = "Booking Amount must be greater than 0")
        Double bookingAmount,
        Integer floorId,
        Integer roomId,
        Integer bedId,
        String bankId,
        String referenceNumber,

        // v2/customers/check-in
        Double advanceAmount,
        Double rentalAmount,
        @Pattern(regexp = "long|short|SHORT|LONG", message = "Type must be either 'long' or 'short'")
        String stayType,
        @Valid
        List<NonRefundable> deductions,
        Boolean proRate
) {}

