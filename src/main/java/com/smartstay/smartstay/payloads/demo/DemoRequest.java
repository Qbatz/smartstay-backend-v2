package com.smartstay.smartstay.payloads.demo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record DemoRequest(
        String countryCode,
        @NotNull(message = "Mobile cannot be empty")
        @NotEmpty(message = "Mobile cannot be empty")
        String mobile,
        @NotNull(message = "Name cannot be empty")
        @NotEmpty(message = "Name cannot be empty")
        String name,
        String emailId,
        String organization,
        Integer noOfProperties,
        Integer noOfTenants,
        @NotNull(message = "City cannot be empty")
        @NotEmpty(message = "City cannot be empty")
        String city,
        String state,
        @NotNull(message = "Date cannot be empty")
        @NotEmpty(message = "Date cannot be empty")
        String requestedDate,
        String requestedTime) {
}
