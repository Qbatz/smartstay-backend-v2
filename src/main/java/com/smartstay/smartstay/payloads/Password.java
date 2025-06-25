package com.smartstay.smartstay.payloads;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record Password(
        @NotBlank(message = "Password is required and cannot be blank")
        @Size(min = 8, max = 100, message = "Password must be at least 8 characters and less than 100")
        String password
) {
}
