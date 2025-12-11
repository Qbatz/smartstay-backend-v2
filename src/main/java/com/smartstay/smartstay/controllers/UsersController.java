package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.Password;
import com.smartstay.smartstay.payloads.VerifyOtpPayloads;
import com.smartstay.smartstay.payloads.account.CreateAccount;
import com.smartstay.smartstay.payloads.account.Login;
import com.smartstay.smartstay.payloads.user.ResetPasswordRequest;
import com.smartstay.smartstay.responses.account.AdminUserResponse;
import com.smartstay.smartstay.services.FCMNotificationService;
import com.smartstay.smartstay.services.UsersService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;

@RestController
@RequestMapping("v2/users")
@CrossOrigin("*")
public class UsersController {

    @Autowired
    UsersService userService;
    @Autowired
    FCMNotificationService fcmNotificationService;

    @PostMapping("")
    public ResponseEntity<AdminUserResponse> createAccount(@Valid @RequestBody CreateAccount createAccount) {
        return userService.createAccount(createAccount);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody Login login) {

        return userService.login(login);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Object> verifyOtp(@RequestBody VerifyOtpPayloads verifyOtp) {
        return userService.verifyOtp(verifyOtp);
    }

    @PostMapping("/verify-password")
    public ResponseEntity<Object> verifyPassword(@RequestBody Password password) {
        return userService.verifyPassword(password);
    }


    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        return userService.verifyOtpAndResetPassword(request);
    }

    @GetMapping("/request-otp/{emailId}")
    public ResponseEntity<Object> requestOtp(@PathVariable("emailId") String emailId) {
        return userService.requestPasswordReset(emailId);
    }

    @GetMapping("/time/zone")
    public ResponseEntity<?> getTimezone() {
        ZonedDateTime now = ZonedDateTime.now();
        return new ResponseEntity<>(now, HttpStatus.OK);
    }



}
