package com.smartstay.smartstay.payloads.customer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public record CheckInRequest(
        @NotNull(message = "First name is required")
        @NotEmpty(message = "First name is required") String firstName,

        String lastName,

        @NotNull(message = "Mobile no is required")
        @NotEmpty(message = "Mobile no is required") String mobile,

        String mailId,
        String houseNo,
        String street,
        String landmark,

        @NotNull(message = "Pincode is required") Integer pincode,

        @NotNull(message = "City is required")
        @NotEmpty(message = "City is required") String city,

        @NotNull(message = "State is required")
        @NotEmpty(message = "State is required") String state,

        @NotNull(message = "HostelId is required")
        @NotEmpty(message = "HostelId is required") String hostelId,

        @NotNull(message = "FloorId is required") Integer floorId,
        @NotNull(message = "BedId is required") Integer bedId,
        @NotNull(message = "RoomId is required") Integer roomId,

        @NotNull(message = "Joining Date is required")
        @NotEmpty(message = "Joining Date is required") String joiningDate
) { }

