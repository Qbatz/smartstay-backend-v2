package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.account.EditUsers;
import com.smartstay.smartstay.payloads.banking.AddBank;
import com.smartstay.smartstay.payloads.banking.UpdateBank;
import com.smartstay.smartstay.services.BankingService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/bank")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class BankingController {

    @Autowired
    BankingService bankingService;

    @PostMapping("/{hostelId}")
    public ResponseEntity<?> addBankAccount(@PathVariable("hostelId") String hostelId, @Valid @RequestBody AddBank addBank) {
        return bankingService.addNewBankAccount(hostelId, addBank);
    }

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getBankAccounts(@PathVariable("hostelId") String hostelId) {
        return bankingService.getAllBankAccounts(hostelId);
    }

    @PutMapping("/{hostelId}/{bankId}")
    public ResponseEntity<?> updateBanking(@PathVariable(value = "hostelId") String hostelId, @PathVariable("bankId") String bankId, @RequestBody(required = false) UpdateBank payloads) {
        return bankingService.updateBankAccount(hostelId, bankId, payloads);
    }

}
