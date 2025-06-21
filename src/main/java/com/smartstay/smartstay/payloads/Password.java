package com.smartstay.smartstay.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record Password(
        @NotEmpty @NotNull String password
) {
}
