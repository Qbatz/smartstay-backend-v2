package com.smartstay.smartstay.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddRoles(@NotNull(message = "Role name is required") @NotEmpty(message = "Role name is required") String roleName) {
}
