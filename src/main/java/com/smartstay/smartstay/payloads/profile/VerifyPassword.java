package com.smartstay.smartstay.payloads.profile;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record VerifyPassword(
        @NotNull(message = "Password is required")
        @NotEmpty(message = "Password is required")
        String password) {
}
