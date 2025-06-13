package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.CreateAccount;
import com.smartstay.smartstay.payloads.Login;
import com.smartstay.smartstay.services.UsersService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UsersController {


    @Autowired
    UsersService userService;


    @PostMapping("/")
    public ResponseEntity<com.smartstay.smartstay.responses.CreateAccount> createAccount(@Valid @RequestBody CreateAccount createAccount) {
        return userService.createAccount(createAccount);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Login login) {

        return userService.login(login);
    }

}
