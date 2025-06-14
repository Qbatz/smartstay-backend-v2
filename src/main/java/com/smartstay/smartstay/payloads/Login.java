package com.smartstay.smartstay.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record Login(@NotNull @NotEmpty String emailId, @NotNull @NotEmpty String password) {
}
