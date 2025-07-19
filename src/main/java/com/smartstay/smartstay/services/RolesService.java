package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.RolesMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.RolesPermission;
import com.smartstay.smartstay.dao.RolesV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.ModuleId;
import com.smartstay.smartstay.payloads.roles.AddRoles;
import com.smartstay.smartstay.payloads.UpdateRoles;
import com.smartstay.smartstay.payloads.roles.Permission;
import com.smartstay.smartstay.repositories.RolesRepository;
import com.smartstay.smartstay.responses.Roles;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
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
    JWTService jwtService;
    @Autowired
    private Authentication authentication;
    @Autowired
    private RolesPermissionServie rolesPermission;
    @Autowired
    private UsersService usersService;


    public ResponseEntity<?> getAllRoles() {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
//        RolesPermission permission = rolesPermission.checkRoleAccess(user.getRoleId(), Utils.MODULE_ID_PROFILE).orElse(null);
        Users users = usersService.findUserByUserId(userId);

        RolesV1 rolesV1 = rolesRepository.findByRoleId(users.getRoleId());

        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        List<RolesPermission> rolesPermissionsList = rolesV1.getPermissions().stream().filter(item -> item.getModuleId() == Utils.MODULE_ID_PAYING_GUEST).toList();
        RolesPermission roles = null;
        if (!rolesPermissionsList.isEmpty()) {
            roles = rolesPermissionsList.get(0);
        }

        if (roles == null || !roles.isCanRead()){
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        List<RolesV1> listRoles = rolesRepository.findAllByParentId(user.getParentId());
        List<Roles> rolesList = listRoles.stream().map(item ->
            new RolesMapper().apply(item)).toList();
        return new ResponseEntity<>(rolesList, HttpStatus.OK);
    }

    public ResponseEntity<?> getRoleById(Integer id) {
        if (id==null || id==0){
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

        List<RolesPermission> rolesPermissionsList = rolesV1.getPermissions().stream().filter(item -> item.getModuleId() == Utils.MODULE_ID_PAYING_GUEST).toList();
        RolesPermission roles = null;
        if (!rolesPermissionsList.isEmpty()) {
            roles = rolesPermissionsList.get(0);
        }
//        RolesPermission permission = rolesPermission.checkRoleAccess(user.getRoleId(), Utils.MODULE_ID_PROFILE).orElse(null);
        if (roles == null || !roles.isCanRead()){
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        RolesV1 v1 = rolesRepository.findById(id).orElse(null);
        if (v1 != null){
            Roles rolesData = new RolesMapper().apply(v1);
            return new ResponseEntity<>(rolesData, HttpStatus.OK);
        }

        return new ResponseEntity<>(Utils.INVALID,HttpStatus.NO_CONTENT);

    }

    public ResponseEntity<?> updateRoleById(int roleId, UpdateRoles updatedRole) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);

//        Users user = usersService.findUserByUserId(userId);
        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());

        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        List<RolesPermission> rolesPermissionsList = rolesV1.getPermissions().stream().filter(item -> item.getModuleId() == Utils.MODULE_ID_PAYING_GUEST).toList();
        RolesPermission roles = null;
        if (!rolesPermissionsList.isEmpty()) {
            roles = rolesPermissionsList.get(0);
        }

//        RolesPermission permission = rolesPermission.checkRoleAccess(user.getRoleId(), Utils.MODULE_ID_PROFILE).orElse(null);
        if (roles == null || !roles.isCanUpdate()){
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        RolesV1 existingRole = rolesRepository.findById(roleId).orElse(null);
        if (existingRole == null) {
            return new ResponseEntity<>(Utils.INVALID,HttpStatus.NO_CONTENT);
        }
        if (updatedRole.roleName()!=null && !updatedRole.roleName().isEmpty()){
            existingRole.setRoleName(updatedRole.roleName());
        }
        if (updatedRole.isActive()!=null){
            existingRole.setIsActive(updatedRole.isActive());
        }
        if (updatedRole.isDeleted()!=null){
            existingRole.setIsDeleted(updatedRole.isDeleted());
        }
        existingRole.setUpdatedAt(new Date());
        rolesRepository.save(existingRole);
        return new ResponseEntity<>(Utils.UPDATED,HttpStatus.OK);

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

        List<RolesPermission> rolesPermissionsList = rolesV1.getPermissions().stream().filter(item -> item.getModuleId() == Utils.MODULE_ID_PAYING_GUEST).toList();
        RolesPermission roles = null;
        if (!rolesPermissionsList.isEmpty()) {
            roles = rolesPermissionsList.get(0);
        }

//        RolesPermission permission = rolesPermission.checkRoleAccess(user.getRoleId(), Utils.MODULE_ID_PROFILE).orElse(null);
        if (roles == null || !roles.isCanWrite()){
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
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
        return new ResponseEntity<>(Utils.CREATED,HttpStatus.CREATED);
    }

    public boolean checkPermission(int roleId, int moduleId, String type) {
        RolesV1 roles = rolesRepository.findByRoleId(roleId);

        if (roles != null) {
            List<RolesPermission> rolesPermission = roles.getPermissions();
            if (!rolesPermission.isEmpty()) {
                List<RolesPermission> filteredPermission = rolesPermission.stream().filter(item -> item.getModuleId() == moduleId).toList();
                System.out.println(filteredPermission.toString());
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
        Map<Integer, Permission> permissionMap = inputPermissions.stream()
                .collect(Collectors.toMap(Permission::moduleId, Function.identity(), (a, b) -> b));

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


}
