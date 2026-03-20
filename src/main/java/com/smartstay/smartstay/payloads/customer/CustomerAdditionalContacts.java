package com.smartstay.smartstay.payloads.customer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CustomerAdditionalContacts(
        @NotEmpty(message = "Full name is required")
        @NotNull(message = "Full name is required")
        String fullName,
        @NotEmpty(message = "Relationship is required")
        @NotNull(message = "Relationship is required̵")
        String relationship,
        String occupation,
        @NotNull(message = "Mobile number is required")
        @NotEmpty(message = "Mobile number is required")
        String mobile) {
}
