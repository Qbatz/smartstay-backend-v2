package com.smartstay.smartstay.responses.customer;

public record TransactionDto(String transactionId,
                             String referenceNumber,
                             String billName,
                             String transactionDate,
                             Double amountPaid,
                             String bankId,
                             String status,
                             String paidTo,
                             String paymentMode) {
}
