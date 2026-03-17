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
    private String startDate;
    private String endDate;
    private Double totalInvoiceAmount;
    private Double receivedAmount;
    private List<ReceiptDetail> receiptsList;
    private String hostelId;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ReceiptDetail {
        private String receiptNo;
        private String Date;
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
    }
}
