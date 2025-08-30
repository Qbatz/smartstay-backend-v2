package com.smartstay.smartstay.responses.roles;

import com.smartstay.smartstay.responses.roles.RolesPermissionDetails;

import java.util.List;

public record Roles(int id, String name, boolean editable, List<RolesPermissionDetails> rolesPermissionDetails) {
}
