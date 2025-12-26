package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.Password;
import com.smartstay.smartstay.payloads.account.*;
import com.smartstay.smartstay.payloads.UpdateUserProfilePayloads;
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
    public ResponseEntity<?> getProfileInformation() {
        return usersService.getProfileInformation();
    }

    @PutMapping("")
    public ResponseEntity<?> updateProfileInformation(@RequestPart("updateProfile") UpdateUserProfilePayloads updateProfile, @RequestPart(value = "profilePic", required = false) MultipartFile profilePic) {
        return usersService.updateProfileInformations(updateProfile, profilePic);
    }

    @PostMapping("/add-admin")
    public ResponseEntity<?> addAdminUser(@Valid @RequestPart("accountInfo") AddAdminPayload createAccount, @RequestPart(value = "profilePic", required = false) MultipartFile profilePic) {
        return usersService.createAdmin(createAccount, profilePic);
    }

    @PostMapping("/add-user/{hostelId}")
    public ResponseEntity<?> addUser(@Valid @RequestBody AddAdminUser adminUser, @PathVariable("hostelId") String hostelId) {
        return usersService.createAdminUser(adminUser, hostelId);
    }

    @PutMapping("/two-step-verification")
    public ResponseEntity<?> updateAuthenticationStatus(@RequestBody UpdateVerificationStatus verificationStatus) {
        return usersService.updateTwoStepVerification(verificationStatus);
    }

    @GetMapping("/admin-list")
    public ResponseEntity<?> getAdminUserList() {
        return usersService.listAllAdmins();
    }

    @GetMapping("/users-list/{hostelId}")
    public ResponseEntity<?> getUserList(@PathVariable("hostelId") String hostelId) {
        return usersService.listAllUsers(hostelId);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Object> changePassword(@Valid @RequestBody Password password) {
        return usersService.changePassword(password);
    }

    @PutMapping("/admin/{adminId}")
    public ResponseEntity<?> updateUdminInformation(@PathVariable(value = "adminId") String adminId, @RequestPart(name = "payload", required = false) EditAdmin payloads, @RequestPart(name = "profilePic", required = false) MultipartFile profilePic) {
        return usersService.updateAdminProfile(adminId, payloads, profilePic);
    }

    @PutMapping("/users/{hostelId}/{userId}")
    public ResponseEntity<?> updateUserInformations(@PathVariable(value = "userId") String userId, @PathVariable("hostelId") String hostelId, @RequestBody(required = false) EditUsers payloads) {
        return usersService.updateUsersProfile(hostelId, userId, payloads);
    }

    @DeleteMapping("/delete-user/{hostelId}/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable("hostelId") String hostelId, @PathVariable("userId") String userId) {
        return usersService.deleteUser(hostelId, userId);
    }


    @DeleteMapping("/delete-admin/{userId}")
    public ResponseEntity<?> deleteAdminUser(@PathVariable("userId") String userId) {
        return usersService.deleteAdminUser(userId);
    }
}
