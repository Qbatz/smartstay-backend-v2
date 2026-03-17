package com.smartstay.smartstay.dto.customer;

public record TransactionDto(String transactionId,
                             String invoiceId,
                             String transctionDate,
                             Double transactionAmount,
                             String referenceNumber,
                             String bankId,
                             String collectedBy) {
}
