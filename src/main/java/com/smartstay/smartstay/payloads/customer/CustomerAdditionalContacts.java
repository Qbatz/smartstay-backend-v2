package com.smartstay.smartstay.payloads.customer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CustomerAdditionalContacts(
        @NotEmpty(message = "Full name is required")
        @NotNull(message = "Full name is required")
        String fullName,
        String relationship,
        String occupation,
        String mobile) {
}
