package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.customer.*;
import com.smartstay.smartstay.payloads.drafts.UpdateDrafts;
import com.smartstay.smartstay.services.CustomersServiceV2;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
                                       @RequestPart(value = "profilePic", required = false) MultipartFile profilePic,
                                       @RequestPart(value = "aadharPic", required = false) MultipartFile aadharPic,
                                       @RequestPart(value = "panPic", required = false) MultipartFile panPic,
                                       @Valid @RequestPart SaveDraftCustomerRequest request) {
        return customersServiceV2.saveDraft(hostelId, profilePic, aadharPic, panPic, request);
    }

    // Update an existing draft. No @Valid on the payload: a draft update enforces no mandatory fields,
    // so every field (including null/empty) is applied to allow clearing previously entered data.
    @PutMapping("/saveDraft/{hostelId}/{customerId}")
    public ResponseEntity<?> updateDraft(@PathVariable("hostelId") String hostelId,
                                         @PathVariable("customerId") String customerId,
                                         @RequestPart(value = "profilePic", required = false) MultipartFile profilePic,
                                         @RequestPart(value = "aadharPic", required = false) MultipartFile aadharPic,
                                         @RequestPart(value = "panPic", required = false) MultipartFile panPic,
                                         @RequestPart SaveDraftCustomerRequest request) {
        return customersServiceV2.updateDraft(hostelId, customerId, profilePic, aadharPic, panPic, request);
    }

    @DeleteMapping("/draft/{hostelId}/{customerId}")
    public ResponseEntity<?> deleteDraft(@PathVariable("hostelId") String hostelId,
                                         @PathVariable("customerId") String customerId) {
        return customersServiceV2.deleteDraft(hostelId, customerId);
    }

    @GetMapping("/draftDetails/{customerId}")
    public ResponseEntity<?> getDraftCustomerDetails(@PathVariable("customerId") String customerId) {
        return customersServiceV2.getDraftCustomerDetails(customerId);
    }

    @PutMapping("/draft/{hostelId}/{customerId}")
    public ResponseEntity<?> updateDraftInformations(@PathVariable("hostelId") String hostelId, @PathVariable("customerId") String customerId, @Valid @RequestBody UpdateDrafts updateDrafts) {
        return customersServiceV2.updateDraftInformations(hostelId, customerId, updateDrafts);
    }

    @PostMapping("/check-in/{hostelId}/{customerId}")
    public ResponseEntity<?> checkInCustomer(@PathVariable("hostelId") String hostelId, @PathVariable("customerId") String customerId, @Valid @RequestBody CheckInRequestNew newCheckInRequest) {
        return customersServiceV2.checkinCustomer(hostelId, customerId, newCheckInRequest);
    }

    @PostMapping("/booked/check-in/{hostelId}/{customerId}")
    public ResponseEntity<?> checkInBookedCustomer(@PathVariable("hostelId") String hostelId, @PathVariable("customerId") String customerId, @Valid @RequestBody CheckInBookedCustomerNew payloads) {
        return customersServiceV2.checkinBookedCustomer(hostelId, customerId, payloads);
    }

    @PutMapping("/additional-details/{hostelId}/{customerId}")
    public ResponseEntity<?> updateCustomerAdditionalData(@PathVariable("hostelId") String hostelId, @PathVariable("customerId") String customerId,
                                                          @RequestPart(required = false) CustomerAdditionalData additionalData,
                                                          @RequestPart(value = "aadhaarPic", required = false) MultipartFile aadhaarPic,
                                                          @RequestPart(value = "panPic", required = false) MultipartFile panPic) {
        return customersServiceV2.addAdditionalData(hostelId, customerId, additionalData, aadhaarPic, panPic);
    }

}
