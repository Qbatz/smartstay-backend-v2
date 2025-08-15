package com.smartstay.smartstay.payloads.vendor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddVendor(
        @NotNull(message = "First name is required") @NotEmpty(message = "First name is required") String firstName,

        String lastName,

        @NotNull(message = "Mobile no is required") @NotEmpty(message = "Mobile no is required") String mobile,

        String mailId, String houseNo, String landmark, String area,

        @NotNull(message = "Pincode is required") Integer pinCode,

        @NotNull(message = "City is required") @NotEmpty(message = "City is required") String city,

        @NotNull(message = "State is required") @NotEmpty(message = "State is required") String state,

        @NotNull(message = "BusinessName is required") @NotEmpty(message = "BusinessName is required") String businessName,

        @NotNull(message = "HostelId is required") @NotEmpty(message = "HostelId is required") String hostelId) {
}
