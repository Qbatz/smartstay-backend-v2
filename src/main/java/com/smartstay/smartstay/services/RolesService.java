package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.RolesMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.RolesPermission;
import com.smartstay.smartstay.dao.RolesV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.payloads.AddRoles;
import com.smartstay.smartstay.payloads.UpdateRoles;
import com.smartstay.smartstay.repositories.RolesRepository;
import com.smartstay.smartstay.responses.Roles;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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
        RolesPermission permission = rolesPermission.checkRoleAccess(user.getRoleId(), Utils.MODULE_ID_PROFILE).orElse(null);
        if (permission == null || !permission.isCanRead()){
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        List<RolesV1> roles = rolesRepository.findAllByParentId(user.getParentId());
        List<Roles> rolesList = roles.stream().map(item ->
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
        RolesPermission permission = rolesPermission.checkRoleAccess(user.getRoleId(), Utils.MODULE_ID_PROFILE).orElse(null);
        if (permission == null || !permission.isCanRead()){
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        RolesV1 roles = rolesRepository.findById(id).orElse(null);
        if (roles!=null){
            Roles rolesData = new RolesMapper().apply(roles);
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
        RolesPermission permission = rolesPermission.checkRoleAccess(user.getRoleId(), Utils.MODULE_ID_PROFILE).orElse(null);
        if (permission == null || !permission.isCanUpdate()){
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
        RolesPermission permission = rolesPermission.checkRoleAccess(user.getRoleId(), Utils.MODULE_ID_PROFILE).orElse(null);
        if (permission == null || !permission.isCanWrite()){
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        RolesV1 role = new RolesV1();
        role.setCreatedAt(new Date());
        role.setUpdatedAt(new Date());
        role.setIsActive(true);
        role.setIsDeleted(false);
        role.setRoleName(roleData.roleName());
        role.setParentId(user.getParentId());
        rolesRepository.save(role);
        return new ResponseEntity<>(Utils.CREATED,HttpStatus.CREATED);
    }


}
