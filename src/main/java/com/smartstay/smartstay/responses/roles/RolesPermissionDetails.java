package com.smartstay.smartstay.responses.roles;

public record RolesPermissionDetails(Integer moduleId,

                                     String moduleName, Boolean canRead, Boolean canWrite, Boolean canDelete,
                                     Boolean canUpdate) {
}
