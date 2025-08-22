package com.smartstay.smartstay.payloads.customer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddCustomer(
        @NotNull(message = "Customer name is required")
        @NotEmpty(message = "Customer name is required")
        String firstName,
        String lastName,
        @NotNull(message = "Mobile number is required")
        @NotEmpty(message = "Mobile number required")
        String mobileNumber,
        String emailId,
        AddCustomerAddress address
) {

}
