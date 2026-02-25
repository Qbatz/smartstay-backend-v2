package com.smartstay.smartstay.responses.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionReportResponse {
    private boolean status;
    private String message;
    private Summary summary;
    private Filters filters;
    private Pagination pagination;
    private List<TransactionData> data;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Summary {
        private String hostelId;
        private String startDate;
        private String endDate;
        private Double totalInvoiceAmount;
        private Double receivedAmount;
        private Double returnedAmount;
        private Double totalTransactionAmount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Filters {
        private List<FilterOption> invoiceType;
        private List<FilterOption> period;
        private List<FilterOption> paymentMode;
        private List<UserFilterOption> collectedBy;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class FilterOption {
        private String id;
        private String label;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UserFilterOption {
        private String user_id;
        private String user_name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Pagination {
        private int currentPage;
        private int pageSize;
        private long totalRecords;
        private int totalPages;
        private boolean hasNextPage;
        private boolean hasPreviousPage;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TransactionData {
        private String receiptNo;
        private String type;
        private Double amount;
        private Double paymentMade;
        private String collectedBy;
        private String bankAccount;
        private String customerName;
        private String bed;
        private String room;
        private String floor;
        private String invoiceNumber;
        private String date;
    }
}
