package com.smartstay.smartstay.dto.transaction;

public record TransactionDto(Integer transactionId,
                             String referenceNumber,
                             Double amount,
                             String type,
                             String source,
                             String createdBy,
                             String createdAt,
                             boolean isCredit) {
}
