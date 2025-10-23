package com.smartstay.smartstay.responses.receipt;

public record ReceiptInfo(String receiptNumber,
                          String receiptId,
                          String transactionDate,
                          Double paidAmount,
                          String particulars,
                          String receivedBy) {
}
