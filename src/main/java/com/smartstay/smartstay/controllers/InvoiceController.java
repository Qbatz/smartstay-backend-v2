package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.invoice.ManualInvoice;
import com.smartstay.smartstay.payloads.invoice.UpdateRecurringInvoice;
import com.smartstay.smartstay.services.InvoiceV1Service;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/bills")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class InvoiceController {

    @Autowired
    private InvoiceV1Service invoiceV1Service;

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getAllTransactions(@PathVariable("hostelId") String hostelId, @RequestParam(value = "startDate", required = false) String startDate, @RequestParam(value = "endDate", required = false) String endDate, @RequestParam(value = "type", required = false) List<String> types, @RequestParam(value = "createdBy", required = false) List<String> createdBy, @RequestParam(value = "modes", required = false) List<String> modes, @RequestParam(value = "search", required = false) String searchKey, @RequestParam(value = "paymentStatus", required = false) List<String> paymentStatus) {
        return invoiceV1Service.getAllInvoices(hostelId, startDate, endDate, types, createdBy, modes, searchKey, paymentStatus);
    }

    @GetMapping("/receipts/{hostelId}")
    public ResponseEntity<?> getAllReceipt(@PathVariable("hostelId") String hostelId) {
        return invoiceV1Service.getAllReceiptsByHostelIdNew(hostelId);
    }

    @PostMapping("/manual/{customerId}")
    public ResponseEntity<?> generateManualInvoice(@PathVariable("customerId") String customerId, @RequestBody @Valid ManualInvoice manualInvoice) {
        return invoiceV1Service.generateManualInvoice(customerId, manualInvoice);
    }

    @GetMapping("/{hostelId}/{invoiceId}")
    public ResponseEntity<?> getInvoiceInfo(@PathVariable("hostelId") String hostelId, @PathVariable("invoiceId") String invoiceId) {
        return invoiceV1Service.getInvoiceDetailsByInvoiceId(hostelId, invoiceId);
    }

    @PutMapping("/{hostelId}/{invoiceId}")
    public ResponseEntity<?> updateRecurringInvoice(@PathVariable("hostelId") String hostelId, @PathVariable("invoiceId") String invoiceId, @RequestBody List<UpdateRecurringInvoice> recurringInvoiceItems) {
        return invoiceV1Service.updateRecurringInvoice(hostelId, invoiceId, recurringInvoiceItems);
    }

    @GetMapping("/refund/{hostelId}/{invoiceId}")
    public ResponseEntity<?> initializeRefunding(@PathVariable("hostelId") String hostelId, @PathVariable("invoiceId") String invoiceId) {
        return invoiceV1Service.initializeRefund(hostelId, invoiceId);
    }

    @PostMapping("/recurring/{hostelId}")
    public ResponseEntity<?> generateRecurringInvoiceManually(@PathVariable("hostelId") String hostelId) {
        return invoiceV1Service.generateRecurringManually(hostelId);
    }

    @GetMapping("/details/{hostelId}/{invoiceId}")
    public ResponseEntity<?> getInvoiceDetails(@PathVariable("hostelId") String hostelId, @PathVariable("invoiceId") String invoiceId) {
        return invoiceV1Service.getInvoiceDetailsForEdit(hostelId, invoiceId);
    }

    @GetMapping("/download/{hostelId}/{invoiceId}")
    public ResponseEntity<?> downloadInvoice(@PathVariable("hostelId") String hostelId, @PathVariable("invoiceId") String invoiceId) {
        return invoiceV1Service.downloadInvoice(hostelId, invoiceId);
    }

    @GetMapping("/share/{hostelId}/{invoiceId}")
    public ResponseEntity<?> shareInvoiceWhatsApp(@PathVariable("hostelId") String hostelId, @PathVariable("invoiceId") String invoiceId) {
        return invoiceV1Service.shareInvoiceWhatsApp(hostelId, invoiceId);
    }

}
