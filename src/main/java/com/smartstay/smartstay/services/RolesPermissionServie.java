package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.RolesPermission;
import com.smartstay.smartstay.repositories.RolesPermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RolesPermissionServie {

    @Autowired
    private RolesPermissionRepository rolesPermissionRepository;

    public List<RolesPermission> getAllPermissionBasedOnRoleId(int roleId) {
        return rolesPermissionRepository.findAllByRoleId(roleId);
    }

    public Optional<RolesPermission> checkRoleAccess(int roleId, int moduleId) {
        return rolesPermissionRepository.findByRoleIdAndModuleId(roleId, moduleId);
    }
}
