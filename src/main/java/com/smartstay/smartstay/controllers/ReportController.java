package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.services.ReportService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/reports")
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getReports(@PathVariable("hostelId") String hostelId) {
        return reportService.getReports(hostelId);
    }

    @GetMapping("/details/{hostelId}")
    public ResponseEntity<?> getInvoiceReportDetails(
            @PathVariable("hostelId") String hostelId,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "paymentStatus", required = false) List<String> paymentStatus,
            @RequestParam(value = "invoiceModes", required = false) List<String> invoiceModes,
            @RequestParam(value = "invoiceTypes", required = false) List<String> invoiceTypes,
            @RequestParam(value = "createdBy", required = false) List<String> createdBy,
            @RequestParam(value = "period", required = false) String period,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return reportService.getInvoiceReportDetails(hostelId, search, paymentStatus, invoiceModes, invoiceTypes,
                createdBy, period, page, size);
    }
}
