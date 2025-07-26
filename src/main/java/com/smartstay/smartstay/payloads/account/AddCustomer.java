package com.smartstay.smartstay.payloads.account;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddCustomer(@NotNull(message = "First name is required")
                          @NotEmpty(message = "First name is required") String firstName,
                          String lastName,
                          @NotNull(message = "Mobile no is required")
                          @NotEmpty(message = "Mobile no is required") String mobile, String mailId, String houseNo, String street, String landmark,
                          @NotNull(message = "pincode is required") int pincode,
                          @NotNull(message = "city is required")
                          @NotEmpty(message = "city is required") String city,
                          @NotNull(message = "state is required")
                          @NotEmpty(message = "state is required") String state) {
}
