package com.smartstay.smartstay.payloads.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record SetupPin(
        @NotNull(message = "PIN is required")
        @Min(value = 1000, message = "PIN must be at least 4 digits")
        @Max(value = 999999, message = "PIN must be at most 6 digits")
        Integer pin) {
}
