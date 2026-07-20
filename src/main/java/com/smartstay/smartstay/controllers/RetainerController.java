package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.retainer.LoadBalance;
import com.smartstay.smartstay.services.RetainerService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/retainer")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class RetainerController {
    @Autowired
    private RetainerService retainerService;

    @PostMapping("/{hostelId}/{customerId}")
    public ResponseEntity<?> addInvoice(@PathVariable("hostelId") String hostelId, @PathVariable("customerId") String customerId, @Valid  @RequestBody LoadBalance loadBalance) {
        return retainerService.addMoney(hostelId, customerId, loadBalance);
    }
}
