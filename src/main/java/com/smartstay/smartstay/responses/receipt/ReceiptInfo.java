package com.smartstay.smartstay.responses.receipt;

public record ReceiptInfo(String receiptNumber,
                          String receiptId,
                          String transactionDate,
                          String transactionTime,
                          Double paidAmount,
                          String particulars,
                          String transactionId,
                          String receivedBy,
                          String invoiceMonth) {
}
