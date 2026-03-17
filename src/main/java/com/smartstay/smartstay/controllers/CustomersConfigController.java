package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.recurring.UpdateRecurring;
import com.smartstay.smartstay.services.CustomersConfigService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/v2/customers/config")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
@RestController
public class CustomersConfigController {

    @Autowired
    private CustomersConfigService customersConfigService;

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getAllCustomers(@PathVariable("hostelId") String hostelId) {
        return customersConfigService.getAllCustomers(hostelId);
    }

    @PutMapping("/{hostelId}/{customerId}")
    public ResponseEntity<?> updateRecurringStatus(@PathVariable("hostelId") String hostelId, @PathVariable("customerId") String customerId, @Valid @RequestBody UpdateRecurring updateRecurring) {
        return customersConfigService.updateStatus(hostelId, customerId, updateRecurring);
    }
}
