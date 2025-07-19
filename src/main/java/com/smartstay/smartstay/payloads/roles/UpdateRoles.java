package com.smartstay.smartstay.payloads.roles;

import com.smartstay.smartstay.payloads.roles.Permission;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateRoles(
        String roleName,
        Boolean isActive,
        Boolean isDeleted,
        @NotNull(message = "Permission list cannot be null")
        @Size(min = 1, max = 19, message = "Permission list must contain between 1 and 19 items")
        List<Permission> permissionList
) {
}
