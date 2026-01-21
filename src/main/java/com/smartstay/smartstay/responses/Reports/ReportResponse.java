package com.smartstay.smartstay.responses.Reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportResponse {
    private String hostelId;
    private Date startDate;
    private Date endDate;
    private InvoiceReport invoices;
    private ReceiptReport receipts;
    private BankingReport banking;
    private TenantReport tenantInfo;
    private ExpenseReport expense;
    private VendorReport vendor;
    private ComplaintReport complaints;
    private RequestReport requests;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class InvoiceReport {
        private int noOfInvoices;
        private Double totalAmount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ReceiptReport {
        private int totalReceipts;
        private Double totalAmount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class BankingReport {
        private int totalTransactions;
        private Double totalAmount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TenantReport {
        private int totalTenants;
        private Double occupancyRate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ExpenseReport {
        private int totalExpenses;
        private Double totalExpenseAmount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class VendorReport {
        private int totalVendors;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ComplaintReport {
        private int totalComplaints;
        private int activeComplaints;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RequestReport {
        private int totalRequests;
        private int activeRequests;
    }
}
