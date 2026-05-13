package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.customer.SaveDraftCustomerRequest;
import com.smartstay.smartstay.services.CustomersServiceV2;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v3/customers")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class CustomersControllerV2 {

    @Autowired
    private CustomersServiceV2 customersServiceV2;

    @GetMapping("/search/{hostelId}")
    public ResponseEntity<?> searchCustomers(@PathVariable("hostelId") String hostelId,
                                             @RequestParam("search") String search) {
        return customersServiceV2.searchCustomersByMobile(hostelId, search);
    }

    @PostMapping("/saveDraft/{hostelId}")
    public ResponseEntity<?> saveDraft(@PathVariable("hostelId") String hostelId,
                                       @Valid @RequestBody SaveDraftCustomerRequest payloads) {
        return customersServiceV2.saveDraft(hostelId, payloads);
    }
}
