package com.smartstay.smartstay.payloads.demo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record DemoRequest(
        String countryCode,
        @NotNull(message = "Mobile cannot empty")
        @NotEmpty(message = "Mobile cannot empty")
        String mobile,
        @NotNull(message = "Name cannot empty")
        @NotEmpty(message = "Name cannot empty")
        String name,
        String emailId,
        String organization,
        Integer noOfProperties,
        Integer noOfTenants,
        @NotNull(message = "City cannot empty")
        @NotEmpty(message = "City cannot empty")
        String city,
        String state,
        @NotNull(message = "Date cannot empty")
        @NotEmpty(message = "Date cannot empty")
        String requestedDate,
        String requestedTime) {
}
