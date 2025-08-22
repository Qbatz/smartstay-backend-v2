package com.smartstay.smartstay.payloads.customer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddCustomerAddress(
        String houseNo,
        String street,
        String landmark,

        @NotNull(message = "Pincode is required") Integer pincode,

        @NotNull(message = "City is required")
        @NotEmpty(message = "City is required") String city,

        @NotNull(message = "State is required")
        @NotEmpty(message = "State is required") String state
) {
}
