package com.smartstay.smartstay.responses.Reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportDetailsResponse {
    private int totalInvoices;
    private int currentPage;
    private int totalPages;
    private Double totalAmount;
    private Double outStandingAmount;
    private Double paidAmount;
    private FilterOptions filterOptions;
    private List<InvoiceDetail> invoiceList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class FilterOptions {
        private List<FilterItem> paymentStatus;
        private List<FilterItem> invoiceModes;
        private List<FilterItem> invoiceTypes;
        private List<UserFilterItem> createdBy;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class FilterItem {
        private String name;
        private String type; // or mode
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UserFilterItem {
        private String name;
        private String userId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class InvoiceDetail {
        private String firstName;
        private String lastName;
        private String fullName;
        private String customerId;
        private String initials;
        private String profilePic;
        private boolean isRefundable;
        private Double invoiceAmount;
        private Double baseAmount;
        private String invoiceId;
        private Double paidAmount;
        private Double dueAmount;
        private Double cgst;
        private Double sgst;
        private Double gst;
        private String createdAt;
        private String createdBy;
        private String hostelId;
        private String invoiceDate;
        private String dueDate;
        private String invoiceType;
        private String invoiceMode;
        private String paymentStatus;
        private String updatedAt;
        private String invoiceNumber;
        private boolean isCancelled;
        private Object listDeductions; // Using Object as it was null in example, refine if structure known
    }
}
