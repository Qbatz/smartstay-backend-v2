package com.smartstay.smartstay.payloads.customer;

import com.smartstay.smartstay.ennum.StayType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Date;
import java.util.List;

public record CheckInRequest(
        @NotNull(message = "HostelId is required")
        @NotEmpty(message = "HostelId is required") String hostelId,

        @NotNull(message = "FloorId is required") Integer floorId,
        @NotNull(message = "BedId is required") Integer bedId,
        @NotNull(message = "RoomId is required") Integer roomId,

        @NotNull(message = "Joining Date is required")
        @NotEmpty(message = "Joining Date is required") String joiningDate,
        @NotNull(message = "Advance amount required")
        Double advanceAmount,
        @NotNull(message = "Advance amount required")
        Double rentalAmount,

        @NotNull(message = "Stay type required")
        @NotEmpty(message = "Stay type required")
        @NotBlank(message = "Stay type is required")
        @Pattern(regexp = "long|short|SHORT|LONG", message = "Type must be either 'long' or 'short'")
        String stayType,

        @Valid
        List<NonRefundable> deductions
) { }

