package com.smartstay.smartstay.payloads.roles;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GetRoles(
        @NotNull Integer role_id
) {
}
