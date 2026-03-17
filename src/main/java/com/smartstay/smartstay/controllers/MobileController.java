package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.account.Login;
import com.smartstay.smartstay.payloads.user.SetupPin;
import com.smartstay.smartstay.payloads.user.VerifyPin;
import com.smartstay.smartstay.services.UsersService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/mobile")
@CrossOrigin("*")
public class MobileController {
    @Autowired
    private UsersService usersService;

    @PostMapping("login")
    public ResponseEntity<?> login(@Valid @RequestBody Login login) {
        return usersService.mobileLogin(login);
    }

    @PostMapping("/pin/{userId}")
    public ResponseEntity<?> setUpPin(@PathVariable("userId") String userId, @Valid @RequestBody SetupPin pin) {
        return usersService.setupPin(userId, pin);
    }

    @PostMapping("/verify/{userId}")
    public ResponseEntity<?> verifyPin(@PathVariable("userId") String userId, @Valid @RequestBody VerifyPin pin) {
        return usersService.verifyPin(userId, pin);
    }

}
