package com.smartstay.smartstay.payloads.roles;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AddRoles(
        @NotNull(message = "Role name is required")
        @NotEmpty(message = "Role name is required")
        String roleName,
        @NotNull(message = "Permission list cannot be null")
        @Size(min = 1, max = 19, message = "Permission list must contain between 1 and 19 items")
        List<Permission> permissionList
) {
}
