package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.banking.AddBankV2;
import com.smartstay.smartstay.services.BankingServiceV2;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v3/bank")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class BankingControllerV2 {

    @Autowired
    private BankingServiceV2 bankingServiceV2;

    // Create a v3 bank / cash account under a hostel.
    @PostMapping("/{hostelId}")
    public ResponseEntity<?> addBank(@PathVariable("hostelId") String hostelId,
            @Valid @RequestBody AddBankV2 payload) {
        return bankingServiceV2.addBank(hostelId, payload);
    }

    // Paginated list of a hostel's bank accounts.
    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getBanks(@PathVariable("hostelId") String hostelId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return bankingServiceV2.getBanks(hostelId, page, size);
    }
}
