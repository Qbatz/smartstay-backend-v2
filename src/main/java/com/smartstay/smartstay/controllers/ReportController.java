package com.smartstay.smartstay.controllers;

import com.smartstay.smartstay.dto.reports.ComplaintsReportFilterRequest;
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
        public ResponseEntity<?> getReports(@PathVariable("hostelId") String hostelId,
                        @RequestParam(value = "startDate", required = false) String startDate,
                        @RequestParam(value = "endDate", required = false) String endDate) {
                return reportService.getReports(hostelId, startDate, endDate);
        }

        @GetMapping("/invoice/{hostelId}")
        public ResponseEntity<?> getInvoiceReportDetails(@PathVariable("hostelId") String hostelId,
                        @RequestParam(value = "search", required = false) String search,
                        @RequestParam(value = "paymentStatus", required = false) List<String> paymentStatus,
                        @RequestParam(value = "invoiceModes", required = false) List<String> invoiceModes,
                        @RequestParam(value = "invoiceTypes", required = false) List<String> invoiceTypes,
                        @RequestParam(value = "createdBy", required = false) List<String> createdBy,
                        @RequestParam(value = "period", required = false) String period,
                        @RequestParam(value = "minPaidAmount", required = false) Double minPaidAmount,
                        @RequestParam(value = "maxPaidAmount", required = false) Double maxPaidAmount,
                        @RequestParam(value = "minOutstandingAmount", required = false) Double minOutstandingAmount,
                        @RequestParam(value = "maxOutstandingAmount", required = false) Double maxOutstandingAmount,
                        @RequestParam(value = "startDate", required = false) String startDate,
                        @RequestParam(value = "endDate", required = false) String endDate,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "10") int size) {
                return reportService.getInvoiceReportDetails(hostelId, search, paymentStatus, invoiceModes,
                                invoiceTypes,
                                createdBy, period, minPaidAmount, maxPaidAmount, minOutstandingAmount,
                                maxOutstandingAmount, startDate,
                                endDate, page, size);
        }

        @GetMapping("/transaction/{hostelId}")
        public ResponseEntity<?> getTransactionReportDetails(@PathVariable("hostelId") String hostelId,
                        @RequestParam(value = "invoiceType", required = false) List<String> invoiceType,
                        @RequestParam(value = "paymentMode", required = false) List<String> paymentMode,
                        @RequestParam(value = "collectedBy", required = false) List<String> collectedBy,
                        @RequestParam(value = "period", required = false) String period,
                        @RequestParam(value = "startDate", required = false) String customStartDate,
                        @RequestParam(value = "endDate", required = false) String customEndDate,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "10") int size) {
                return reportService.getReceiptDetails(hostelId, period, customStartDate, customEndDate, invoiceType,
                                paymentMode, collectedBy, page, size);
        }

        @GetMapping("/expense/{hostelId}")
        public ResponseEntity<?> getExpenseReportDetails(@PathVariable("hostelId") String hostelId,
                        @RequestParam(value = "period", required = false) String period,
                        @RequestParam(value = "startDate", required = false) String customStartDate,
                        @RequestParam(value = "endDate", required = false) String customEndDate,
                        @RequestParam(value = "categoryId", required = false) List<Long> categoryId,
                        @RequestParam(value = "subCategoryId", required = false) List<Long> subCategoryId,
                        @RequestParam(value = "paymentMode", required = false) List<String> paymentMode,
                        @RequestParam(value = "paidTo", required = false) List<String> paidTo,
                        @RequestParam(value = "createdBy", required = false) List<String> createdBy,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "10") int size) {
                return reportService.getExpenseDetails(hostelId, period, customStartDate, customEndDate, categoryId,
                                subCategoryId, paymentMode, paidTo, createdBy, page, size);
        }

        @GetMapping("/tenants/{hostelId}")
        public ResponseEntity<?> getTenantRegisterDetails(@PathVariable("hostelId") String hostelId,
                        @RequestParam(value = "search", required = false) String search,
                        @RequestParam(value = "status", required = false) List<String> status,
                        @RequestParam(value = "room", required = false) List<Integer> room,
                        @RequestParam(value = "floor", required = false) List<Integer> floor,
                        @RequestParam(value = "period", required = false) String period,
                        @RequestParam(value = "startDate", required = false) String startDate,
                        @RequestParam(value = "endDate", required = false) String endDate,
                        @RequestParam(value = "sharingType", required = false) List<String> sharingType,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "10") int size) {
                return reportService.getTenantRegister(hostelId, search, status, room, floor, period, startDate,
                                endDate, page,
                                size, sharingType);
        }

        @PostMapping("/complaints/{hostelId}")
        public ResponseEntity<?> getComplaintsReport(@PathVariable("hostelId") String hostelId,
                        @RequestBody ComplaintsReportFilterRequest request) {
                return reportService.getComplaintsReport(hostelId, request);
        }
}
