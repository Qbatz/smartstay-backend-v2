package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.services.InvoiceV1Service;
import com.smartstay.smartstay.services.TransactionService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/bills")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class InvoiceController {

    @Autowired
    private InvoiceV1Service invoiceV1Service;

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getAllTransactions(@PathVariable("hostelId") String hostelId) {
        return invoiceV1Service.getTransactions(hostelId);
    }
}
