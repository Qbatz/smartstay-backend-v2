package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.Modules;
import com.smartstay.smartstay.dao.RolesPermission;
import com.smartstay.smartstay.dao.RolesV1;
import com.smartstay.smartstay.repositories.ModulesRepository;
import com.smartstay.smartstay.responses.roles.Roles;
import com.smartstay.smartstay.responses.roles.RolesPermissionDetails;
import com.smartstay.smartstay.util.Utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RolesMapper implements Function<RolesV1, Roles> {

    private final ModulesRepository modulesRepository;
    private final Map<Integer, Long> userCountMap;

    public RolesMapper(ModulesRepository modulesRepository) {
        this.modulesRepository = modulesRepository;
        this.userCountMap = Collections.emptyMap();
    }

    public RolesMapper(ModulesRepository modulesRepository, Map<Integer, Long> userCountMap) {
        this.modulesRepository = modulesRepository;
        this.userCountMap = userCountMap;
    }

    @Override
    public Roles apply(RolesV1 rolesV1) {
        String roleName = null;
        List<RolesPermission> rolePermissions = rolesV1.getPermissions();
        long userCount = userCountMap.getOrDefault(rolesV1.getRoleId(), 0L);
        String createdAt = Utils.dateToDateTime(rolesV1.getCreatedAt());

        if (rolePermissions == null || rolePermissions.isEmpty()) {
            return new Roles(rolesV1.getRoleId(), rolesV1.getRoleName(), rolesV1.getIsEditable(), Collections.emptyList(),
                    rolesV1.getDescription(), userCount, createdAt);
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

        if (rolesV1.getRoleId() == 1 || rolesV1.getRoleId() == 2) {
            roleName = "Admin";
        }
        else {
            roleName = rolesV1.getRoleName();
        }
        return new Roles(rolesV1.getRoleId(), roleName, rolesV1.getIsEditable(), permissionDetails,
                rolesV1.getDescription(), userCount, createdAt);
    }

}
