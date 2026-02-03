package com.smartstay.smartstay.responses.receiptForReport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReceiptReportResponse {
    private Double totalRentCollected;
    private BankCollectionInfo highestCollectingBank;
    private int currentPage;
    private int totalPages;
    private BasicReceiptDetails basicDetails;
    private List<ReceiptDetail> receipts;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class BankCollectionInfo {
        private String bankName;
        private Double totalAmount;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class BasicReceiptDetails {
        private int totalReceipts;
        private String startDate;
        private String endDate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ReceiptDetail {
        private String transactionId;
        private String customerName;
        private String paymentDate;
        private Double amount;
        private String bankName;
        private String transactionType;
        private String transactionReferenceId;
        private String referenceNumber;
        private String status;
    }
}
