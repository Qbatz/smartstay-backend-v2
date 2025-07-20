package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.account.CreateAccount;
import com.smartstay.smartstay.payloads.account.Login;
import com.smartstay.smartstay.payloads.Password;
import com.smartstay.smartstay.payloads.VerifyOtpPayloads;
import com.smartstay.smartstay.services.UsersService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/users")
@CrossOrigin("*")
public class UsersController {

    @Autowired
    UsersService userService;

    @PostMapping("")
    public ResponseEntity<com.smartstay.smartstay.responses.CreateAccount> createAccount(@Valid @RequestBody CreateAccount createAccount) {
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

    @PostMapping("/change-password")
    public ResponseEntity<Object> changePassword(@RequestBody Password password) {
        return userService.changePassword(password);
    }


}
