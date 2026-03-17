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
    private String startDate;
    private String endDate;
    private Double outStandingAmount;
    private Double totalRevenue;
    private InvoiceReport invoices;
    private ReceiptReport receipts;
    private BankingReport banking;
    private TenantReport tenantInfo;
    private ExpenseReport expense;
    private VendorReport vendor;
    private ComplaintReport complaints;
    private RequestReport requests;
    private ElectricityReport electricity;
    private FinalSettlementReport settlement;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class InvoiceReport {
        private int noOfInvoices;
        private Double totalAmount;
        private Double paidAmount;
        private Double outstandingAmount;
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
        private int activeTenantCount;
        private int noticeTenantCount;
        private int checkoutTenantsCount;
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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ElectricityReport {
        private int totalEntries;
        private Double totalUnits;
        private Double totalAmount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class FinalSettlementReport {
        private int totalSettlements;
        private Double totalReturnedAmount;
        private Double totalPaidAmount;
        private Double totalAmount;
    }
}
