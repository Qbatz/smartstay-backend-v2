package com.smartstay.smartstay.payloads.customer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddCustomerPartialInfo(
        @NotNull(message = "First name is required")
        @NotEmpty(message = "First name is required")
        String firstName,
                                     String lastName,
                                     @NotNull(message = "Mobile number required")
                                             @NotEmpty(message = "Mobile number required")
                                     String mobile,
                                     String emailId) {
}
