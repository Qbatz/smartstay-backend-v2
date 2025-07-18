package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.AddRoles;
import com.smartstay.smartstay.payloads.UpdateRoles;
import com.smartstay.smartstay.services.RolesService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/role")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
public class RolesController {

    @Autowired
    private RolesService rolesService;


    @GetMapping("")
    public ResponseEntity<?> getAllRoles() {
        return rolesService.getAllRoles();
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<?> getRoleById(@PathVariable("roleId") int roleId) {
        return rolesService.getRoleById(roleId);
    }


    @PostMapping("")
    public ResponseEntity<?> addRole(@Valid @RequestBody AddRoles roleDto) {
        return rolesService.addRole(roleDto);
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<?> updateRole(@PathVariable("roleId") int roleId, @RequestBody UpdateRoles updateRoles) {
        return rolesService.updateRoleById(roleId, updateRoles);
    }
}
