package com.smartstay.smartstay.payloads.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SetupPin(
        @NotNull(message = "PIN is required")
        @NotEmpty(message = "PIN is required")
        String pin,
        String platform) {
}
