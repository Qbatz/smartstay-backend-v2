package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.RolesMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.Modules;
import com.smartstay.smartstay.dao.RolesPermission;
import com.smartstay.smartstay.dao.RolesV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.ModuleId;
import com.smartstay.smartstay.payloads.roles.AddRoles;
import com.smartstay.smartstay.payloads.roles.Permission;
import com.smartstay.smartstay.payloads.roles.UpdateRoles;
import com.smartstay.smartstay.repositories.ModulesRepository;
import com.smartstay.smartstay.repositories.RolesRepository;
import com.smartstay.smartstay.responses.roles.Roles;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RolesService {
    @Autowired
    RolesRepository rolesRepository;
    @Autowired
    ModulesRepository modulesRepository;
    @Autowired
    private Authentication authentication;

    private UsersService usersService;

    @Autowired
    public void setUsersService(@Lazy  UsersService usersService) {
        this.usersService = usersService;
    }

    public ResponseEntity<?> getAllRoles() {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        Users users = usersService.findUserByUserId(userId);
        RolesV1 rolesV1 = rolesRepository.findByRoleId(users.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!checkPermission(user.getRoleId(), Utils.MODULE_ID_PROFILE, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        List<RolesV1> listRoles = rolesRepository.findAllByParentId(user.getParentId());
        List<Roles> rolesList = listRoles.stream().map(item -> new RolesMapper(modulesRepository).apply(item)).toList();
        return new ResponseEntity<>(rolesList, HttpStatus.OK);
    }

    public ResponseEntity<?> getRoleById(Integer id) {
        if (id == null || id == 0) {
            return new ResponseEntity<>(Utils.INVALID, HttpStatus.NO_CONTENT);
        }
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());

        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!checkPermission(user.getRoleId(), Utils.MODULE_ID_PROFILE, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        RolesV1 v1 = rolesRepository.findByRoleIdAndParentId(id,user.getParentId());
        if (v1 != null) {
            Roles rolesData = new RolesMapper(modulesRepository).apply(v1);
            return new ResponseEntity<>(rolesData, HttpStatus.OK);
        }

        return new ResponseEntity<>(Utils.INVALID, HttpStatus.NO_CONTENT);

    }

    public ResponseEntity<?> updateRoleById(int roleId, UpdateRoles updatedRole) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!checkPermission(user.getRoleId(), Utils.MODULE_ID_PROFILE, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        RolesV1 existingRole = rolesRepository.findByRoleIdAndParentId(roleId,user.getParentId());
        if (existingRole == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (updatedRole.roleName() != null && !updatedRole.roleName().isEmpty()) {
            existingRole.setRoleName(updatedRole.roleName());
        }
        if (updatedRole.isActive() != null) {
            existingRole.setIsActive(updatedRole.isActive());
        }
        if (updatedRole.permissionList() != null && !updatedRole.permissionList().isEmpty()) {
            Map<Integer, Permission> incomingPermissions = updatedRole.permissionList().stream().collect(Collectors.toMap(Permission::moduleId, Function.identity(), (a, b) -> b));

            List<RolesPermission> finalPermissions = Arrays.stream(ModuleId.values()).map(module -> updatePermission(module.getId(), incomingPermissions, existingRole.getPermissions())).collect(Collectors.toList());

            existingRole.setPermissions(finalPermissions);
        }
        existingRole.setUpdatedAt(new Date());
        rolesRepository.save(existingRole);
        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);

    }

    public ResponseEntity<?> addRole(AddRoles roleData) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!checkPermission(user.getRoleId(), Utils.MODULE_ID_PROFILE, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (rolesRepository.existsByParentIdAndRoleName(roleData.roleName(), user.getParentId()) > 0) {
            return new ResponseEntity<>(Utils.ROLE_NAME_EXISTS, HttpStatus.BAD_REQUEST);
        }

        RolesV1 role = new RolesV1();
        List<RolesPermission> rolesPermissions = permissionInsertion(roleData.permissionList());
        role.setCreatedAt(new Date());
        role.setUpdatedAt(new Date());
        role.setIsActive(true);
        role.setIsDeleted(false);
        role.setRoleName(roleData.roleName());
        role.setParentId(user.getParentId());
        role.setPermissions(rolesPermissions);
        rolesRepository.save(role);
        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    public ResponseEntity<?> deleteRoleById(int roleId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users users = usersService.findUserByUserId(userId);
        if (!checkPermission(users.getRoleId(), Utils.MODULE_ID_PROFILE, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (usersService.findActiveUsersByRole(roleId) != null && !usersService.findActiveUsersByRole(roleId).isEmpty()) {
            return new ResponseEntity<>(Utils.ACTIVE_USERS_FOUND, HttpStatus.BAD_REQUEST);
        }
        RolesV1 existingRole = rolesRepository.findByRoleIdAndParentId(roleId,users.getParentId());
        if (existingRole != null) {
            rolesRepository.delete(existingRole);
            return new ResponseEntity<>("Deleted", HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>("No Roles found", HttpStatus.BAD_REQUEST);

    }

    public boolean checkPermission(int roleId, int moduleId, String type) {
        RolesV1 roles = rolesRepository.findByRoleId(roleId);

        if (roles != null) {
            List<RolesPermission> rolesPermission = roles.getPermissions();
            if (!rolesPermission.isEmpty()) {
                List<RolesPermission> filteredPermission = rolesPermission.stream().filter(item -> item.getModuleId() == moduleId).toList();
                if (!filteredPermission.isEmpty()) {
                    if (type.equalsIgnoreCase(Utils.PERMISSION_READ)) {
                        return filteredPermission.get(0).isCanRead();
                    }
                    if (type.equalsIgnoreCase(Utils.PERMISSION_WRITE)) {
                        return filteredPermission.get(0).isCanWrite();
                    }
                    if (type.equalsIgnoreCase(Utils.PERMISSION_UPDATE)) {
                        return filteredPermission.get(0).isCanUpdate();
                    }
                    if (type.equalsIgnoreCase(Utils.PERMISSION_DELETE)) {
                        return filteredPermission.get(0).isCanDelete();
                    }
                }
            }
        }

        return false;
    }

    private List<RolesPermission> permissionInsertion(List<Permission> inputPermissions) {
        Map<Integer, Permission> permissionMap = inputPermissions.stream().collect(Collectors.toMap(Permission::moduleId, Function.identity(), (a, b) -> b));

        List<RolesPermission> result = new ArrayList<>();

        for (ModuleId module : ModuleId.values()) {
            Permission p = permissionMap.get(module.getId());
            RolesPermission rp = new RolesPermission();
            rp.setModuleId(module.getId());
            rp.setCanRead(p != null && Boolean.TRUE.equals(p.canRead()));
            rp.setCanWrite(p != null && Boolean.TRUE.equals(p.canWrite()));
            rp.setCanUpdate(p != null && Boolean.TRUE.equals(p.canUpdate()));
            rp.setCanDelete(p != null && Boolean.TRUE.equals(p.canDelete()));
            result.add(rp);
        }

        return result;
    }


    private RolesPermission updatePermission(int moduleId, Map<Integer, Permission> incomingPermissions, List<RolesPermission> existingPermissions) {
        Permission incoming = incomingPermissions.get(moduleId);

        RolesPermission existingDB = existingPermissions.stream().filter(p -> p.getModuleId() == moduleId).findFirst().orElse(new RolesPermission());

        RolesPermission merged = new RolesPermission();
        merged.setModuleId(moduleId);
        merged.setCanRead(incoming != null && incoming.canRead() != null ? incoming.canRead() : existingDB.isCanRead());
        merged.setCanWrite(incoming != null && incoming.canWrite() != null ? incoming.canWrite() : existingDB.isCanWrite());
        merged.setCanUpdate(incoming != null && incoming.canUpdate() != null ? incoming.canUpdate() : existingDB.isCanUpdate());
        merged.setCanDelete(incoming != null && incoming.canDelete() != null ? incoming.canDelete() : existingDB.isCanDelete());

        return merged;
    }

    public boolean checkRoleId(int roleId) {
        return rolesRepository.existsByRoleId(roleId);
    }
  
    public String findById(int roleId) {
        RolesV1 rolesV1 = rolesRepository.findById(roleId).orElse(null);
        if (rolesV1 != null) {
            return rolesV1.getRoleName();
        }
        return null;
    }

    public ResponseEntity<?> getAllModules() {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        List<Modules> listModules = modulesRepository.findAll();
        return new ResponseEntity<>(listModules, HttpStatus.OK);
    }
}
