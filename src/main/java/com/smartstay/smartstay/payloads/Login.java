package com.smartstay.smartstay.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record Login(@NotNull(message = "Email Id is required") String emailId, @NotNull(message = "Password is required") String password) {
}
