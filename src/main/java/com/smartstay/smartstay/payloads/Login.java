package com.smartstay.smartstay.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record Login(@NotNull(message = "Email Id is required") @NotEmpty(message = "Email Id cannot be empty") String emailId, @NotNull(message = "Password is required") @NotEmpty(message = "Password cannot be empty") String password) {
}
