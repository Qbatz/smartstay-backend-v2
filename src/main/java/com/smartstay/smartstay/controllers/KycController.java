package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.services.KycServices;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/kyc")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class KycController {
    @Autowired
    private KycServices kycServices;

    @PostMapping("/request/{customerId}")
    public ResponseEntity<?> requestKyc(@PathVariable("customerId") String customerId) {
        return kycServices.requestKycService(customerId);
    }
}
