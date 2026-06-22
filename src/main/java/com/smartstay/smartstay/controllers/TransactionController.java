package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.payloads.invoice.RefundInvoice;
import com.smartstay.smartstay.payloads.transactions.AddPayment;
import com.smartstay.smartstay.services.TransactionService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getAllReceipts(@PathVariable("hostelId") String hostelId,
                                            @RequestParam(value = "keyword", required = false) String keyword,
                                            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
                                            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                            @RequestParam(value = "period", required = false) String period,
                                            @RequestParam(value = "bankIds", required = false) List<String> bankIds) {
        return transactionService.getAllReceiptsByHostelIdNew(hostelId, keyword,bankIds, size, page, period);
    }
    @PostMapping("/{hostelId}/{invoiceId}")
    public ResponseEntity<?> recordPayment(@PathVariable("hostelId") String hostelId, @PathVariable("invoiceId") String invoiceId, @Valid  @RequestBody AddPayment addPayment) {
        return transactionService.recordPayment(hostelId, invoiceId, addPayment);
    }

    @GetMapping("/{hostelId}/{transactionId}")
    public ResponseEntity<?> getReceiptDetails(@PathVariable("hostelId") String hostelId, @PathVariable("transactionId") String transactionId) {
        return transactionService.getReceiptDetailsByTransactionId(hostelId, transactionId);
    }

    @PostMapping("/refund/{hostelId}/{invoiceId}")
    public ResponseEntity<?> refundInvoice(@PathVariable("hostelId") String hostelId, @PathVariable("invoiceId") String invoiceId, @RequestBody @Valid RefundInvoice refundInvoice) {
        return transactionService.refundForInvoice(hostelId, invoiceId, refundInvoice);
    }
    @DeleteMapping("/receipts/{hostelId}/{transactionId}")
    public ResponseEntity<?> deleteReceipt(@PathVariable("hostelId") String hostelId, @PathVariable("transactionId") String receiptId) {
        return transactionService.deleteReceipt(hostelId, receiptId);
    }

    @GetMapping("/download/{hostelId}/{transactionId}")
    public ResponseEntity<?> downloadReceipts(@PathVariable("hostelId") String hostelId,
                                              @PathVariable("transactionId") String transactionId) {
        return transactionService.downloadRecipt(hostelId, transactionId);
    }

    @GetMapping("/share/{hostelId}/{transactionId}")
    public ResponseEntity<?> shareReceiptWhatsApp(@PathVariable("hostelId") String hostelId,
                                                  @PathVariable("transactionId") String transactionId) {
        return transactionService.shareReceiptWhatsApp(hostelId, transactionId);
    }
}
