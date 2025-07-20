package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.Modules;
import com.smartstay.smartstay.dao.RolesPermission;
import com.smartstay.smartstay.dao.RolesV1;
import com.smartstay.smartstay.repositories.ModulesRepository;
import com.smartstay.smartstay.responses.roles.Roles;
import com.smartstay.smartstay.responses.roles.RolesPermissionDetails;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RolesMapper implements Function<RolesV1, Roles> {

    private final ModulesRepository modulesRepository;

    public RolesMapper(ModulesRepository modulesRepository) {
        this.modulesRepository = modulesRepository;
    }

    @Override
    public Roles apply(RolesV1 rolesV1) {
        List<RolesPermission> rolePermissions = rolesV1.getPermissions();
        if (rolePermissions == null || rolePermissions.isEmpty()) {
            return new Roles(rolesV1.getRoleId(), rolesV1.getRoleName(), Collections.emptyList());
        }
        Set<Integer> moduleIds = rolePermissions.stream()
                .map(RolesPermission::getModuleId)
                .collect(Collectors.toSet());
        Map<Integer, String> moduleIdToName = modulesRepository.findAllById(moduleIds).stream()
                .collect(Collectors.toMap(Modules::getId, Modules::getModuleName));
        List<RolesPermissionDetails> permissionDetails = rolePermissions.stream()
                .map(p -> new RolesPermissionDetails(
                        p.getModuleId(),
                        moduleIdToName.getOrDefault(p.getModuleId(), "Unknown"),
                        p.isCanRead(),
                        p.isCanWrite(),
                        p.isCanDelete(),
                        p.isCanUpdate()
                ))
                .toList();

        return new Roles(rolesV1.getRoleId(), rolesV1.getRoleName(), permissionDetails);
    }

}

