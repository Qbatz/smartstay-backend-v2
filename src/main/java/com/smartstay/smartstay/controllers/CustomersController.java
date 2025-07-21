package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.account.AddCustomer;
import com.smartstay.smartstay.services.CustomersService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("v2/customers")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
public class CustomersController {

    @Autowired
    private CustomersService customersService;

    @PostMapping("")
    public ResponseEntity<?> getAllCustomers(@RequestPart MultipartFile file, @RequestPart AddCustomer payloads) {
        return customersService.createCustomer(file, payloads);
    }
 }
