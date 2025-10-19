package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.transactions.AddPayment;
import com.smartstay.smartstay.services.TransactionService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2/transaction")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class TransactionController {

    @Autowired
    TransactionService transactionService;

    @PostMapping("/{hostelId}/{invoiceId}")
    public ResponseEntity<?> recordPayment(@PathVariable("hostelId") String hostelId, @PathVariable("invoiceId") String invoiceId, @Valid  @RequestBody AddPayment addPayment) {
        return transactionService.recordPayment(hostelId, invoiceId, addPayment);
    }

    @GetMapping("/{hostelId}/{transactionId}")
    public ResponseEntity<?> getReceiptDetails(@PathVariable("hostelId") String hostelId, @PathVariable("transactionId") String transactionId) {
        return transactionService.getReceiptDetailsByTransactionId(hostelId, transactionId);
    }
}
