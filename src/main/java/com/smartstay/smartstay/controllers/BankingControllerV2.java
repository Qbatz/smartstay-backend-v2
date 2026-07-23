package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.banking.AddBankV2;
import com.smartstay.smartstay.payloads.banking.AddBankingMethod;
import com.smartstay.smartstay.services.BankingServiceV2;
import com.smartstay.smartstay.services.QrBankTypeService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("v3/bank")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class BankingControllerV2 {

    @Autowired
    private BankingServiceV2 bankingServiceV2;

    @Autowired
    private QrBankTypeService qrBankTypeService;

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

    // Users (with roles) mapped to the hostel, for the CASH account "responsible person" picker.
    @GetMapping("/responsiblePerson/{hostelId}")
    public ResponseEntity<?> getResponsiblePersons(@PathVariable("hostelId") String hostelId) {
        return bankingServiceV2.getResponsiblePersons(hostelId);
    }

    @PostMapping(value = "/qrCardType", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addQrCardType(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return qrBankTypeService.create(type, name, image);
    }

    @GetMapping("/qrCardType")
    public ResponseEntity<?> getQrCardTypes(@RequestParam(value = "type", required = false) String type) {
        return qrBankTypeService.getAll(type);
    }

    @GetMapping("/qrCardType/{id}")
    public ResponseEntity<?> getQrCardType(@PathVariable("id") Integer id) {
        return qrBankTypeService.getById(id);
    }

    @PutMapping(value = "/qrCardType/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateQrCardType(
            @PathVariable("id") Integer id,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        return qrBankTypeService.update(id, type, name, image);
    }

    @DeleteMapping("/qrCardType/{id}")
    public ResponseEntity<?> deleteQrCardType(@PathVariable("id") Integer id) {
        return qrBankTypeService.delete(id);
    }

    @PostMapping(value = "/bankMethod/{hostelId}/{bankId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addBankingMethod(
            @PathVariable("hostelId") String hostelId,
            @PathVariable("bankId") String bankId,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
            @RequestParam(value = "upiId", required = false) String upiId,
            @RequestParam(value = "upiApp", required = false) Integer upiApp,
            @RequestParam(value = "displayName", required = false) String displayName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "cardNumber", required = false) String cardNumber,
            @RequestParam(value = "cardNetwork", required = false) Integer cardNetwork,
            @RequestParam(value = "cardHolderName", required = false) String cardHolderName,
            @RequestParam(value = "creditLimit", required = false) Double creditLimit,
            @RequestParam(value = "billingCycle", required = false) String billingCycle,
            @RequestParam(value = "linkedUpiId", required = false) String linkedUpiId,
            @RequestParam(value = "qrImage", required = false) MultipartFile qrImage) {
        AddBankingMethod payload = new AddBankingMethod(paymentMethod, upiId, upiApp, displayName,
                description, cardNumber, cardNetwork, cardHolderName, creditLimit, billingCycle, linkedUpiId);
        return bankingServiceV2.addBankingMethod(hostelId, bankId, payload, qrImage);
    }

    @GetMapping("/bankMethod/{hostelId}/{bankId}")
    public ResponseEntity<?> getBankingMethods(
            @PathVariable("hostelId") String hostelId,
            @PathVariable("bankId") String bankId) {
        return bankingServiceV2.getBankingMethods(hostelId, bankId);
    }
}
