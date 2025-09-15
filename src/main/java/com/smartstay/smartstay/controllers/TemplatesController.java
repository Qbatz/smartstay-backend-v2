package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.banking.UpdateBank;
import com.smartstay.smartstay.payloads.billTemplate.UpdateBillTemplate;
import com.smartstay.smartstay.payloads.billTemplate.UpdateBillingRule;
import com.smartstay.smartstay.services.TemplatesService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v2/hostel/config")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class TemplatesController {

    @Autowired
    private TemplatesService templateService;

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getTemplates(@PathVariable("hostelId") String hostelId) {
        return templateService.getTemplates(hostelId);
    }
    @PostMapping("/{hostelId}")
    public ResponseEntity<?> updateTemplates(
            @PathVariable("hostelId") String hostelId,
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "isMobileCustomized", required = false) Boolean isMobileCustomized,
            @RequestParam(value = "isEmailCustomized", required = false) Boolean isEmailCustomized,
            @RequestParam(value = "isLogoCustomized", required = false) Boolean isLogoCustomized,
            @RequestParam(value = "isSignatureCustomized", required = false) Boolean isSignatureCustomized,
            @RequestPart(value = "hostelLogo", required = false) MultipartFile hostelLogo,
            @RequestPart(value = "billSignature", required = false) MultipartFile billSignature,
            @RequestPart(value = "invLogo", required = false) MultipartFile invLogo,
            @RequestPart(value = "invSign", required = false) MultipartFile invSign,
            @RequestPart(value = "qrCode", required = false) MultipartFile qrCode,
            @RequestPart(value = "receiptLogo", required = false) MultipartFile receiptLogo,
            @RequestPart(value = "receiptSign", required = false) MultipartFile receiptSign,
            @RequestPart(value = "request",required = false) UpdateBillTemplate payloads
    ) {
        return templateService.updateTemplate(hostelId, mobile, email, isMobileCustomized, isEmailCustomized,
                isLogoCustomized,
                isSignatureCustomized,
                hostelLogo, billSignature, invLogo, invSign, qrCode, receiptLogo, receiptSign,
                payloads);
    }

    @PostMapping("billing-rule/{hostelId}/{billingRuleId}")
    public ResponseEntity<?> updateBillingRule(
            @PathVariable("hostelId") String hostelId,
            @PathVariable("billingRuleId") Integer billingRuleId,
            @Valid @RequestBody UpdateBillingRule payloads
    ) {
        return templateService.updateBillingRule(hostelId, billingRuleId,
                payloads);
    }


    @GetMapping("billing-rule/{hostelId}")
    public ResponseEntity<?> getBillingRule(
            @PathVariable("hostelId") String hostelId
    ) {
        return templateService.getBillingRule(hostelId);
    }


}
