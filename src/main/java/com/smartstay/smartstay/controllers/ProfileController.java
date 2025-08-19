package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.account.AddAdminPayload;
import com.smartstay.smartstay.payloads.UpdateUserProfilePayloads;
import com.smartstay.smartstay.payloads.account.AddAdminUser;
import com.smartstay.smartstay.services.UsersService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("v2/profile")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class ProfileController {

    @Autowired
    UsersService usersService;

    @GetMapping("")
    public ResponseEntity<Object> getProfileInformation() {
        return usersService.getProfileInformation();
    }

    @PutMapping("")
    public ResponseEntity<Object> updateProfileInformation(@RequestPart("updateProfile") UpdateUserProfilePayloads updateProfile, @RequestPart("profilePic") MultipartFile profilePic) {
        return usersService.updateProfileInformations(updateProfile, profilePic);
    }

    @PostMapping("/add-admin")
    public ResponseEntity<?> addAdminUser(@Valid @RequestPart("accountInfo") AddAdminPayload createAccount, @RequestPart("profilePic") MultipartFile profilePic) {
        return usersService.createAdmin(createAccount, profilePic);
    }

    @PostMapping("/add-user")
    public ResponseEntity<?> addUser(@Valid @RequestBody AddAdminUser adminUser) {
        return usersService.createAdminUser(adminUser);
    }
}
