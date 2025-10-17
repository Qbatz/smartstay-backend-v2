package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.banking.AddBank;
import com.smartstay.smartstay.payloads.banking.SelfTransfer;
import com.smartstay.smartstay.payloads.banking.UpdateBank;
import com.smartstay.smartstay.payloads.banking.UpdateBankBalance;
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


    @PutMapping("/add-balance/{hostelId}")
    public ResponseEntity<?> addBankBalance(@PathVariable(value = "hostelId") String hostelId, @RequestBody(required = false) UpdateBankBalance payloads) {
        return bankingService.updateBankBalance(hostelId, payloads);
    }

    @PutMapping("/add-money/{hostelId}")
    public ResponseEntity<?> addMoney(@PathVariable(value = "hostelId") String hostelId, @RequestBody(required = false) UpdateBankBalance payloads) {
        return bankingService.addMoney(hostelId, payloads);
    }

    @PutMapping("/self-transfer/{hostelId}")
    public ResponseEntity<?> selfTransfer(@PathVariable(value = "hostelId") String hostelId, @RequestBody(required = false) SelfTransfer payloads) {
        return bankingService.selfTransfer(hostelId, payloads);
    }

}
