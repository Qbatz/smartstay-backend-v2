package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.UpdateUserProfilePayloads;
import com.smartstay.smartstay.services.UsersService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
public class ProfileController {

    @Autowired
    UsersService usersService;

    @GetMapping("/")
    public ResponseEntity<Object> getProfileInformation() {
        return usersService.getProfileInformation();
    }

    @PutMapping("/")
    public ResponseEntity<Object> updateProfileInformation(@RequestBody UpdateUserProfilePayloads updateProfile) {
        return usersService.updateProfileInformations(updateProfile);
    }
}
