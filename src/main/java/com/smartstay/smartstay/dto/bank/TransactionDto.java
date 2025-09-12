package com.smartstay.smartstay.dto.bank;

public record TransactionDto(String bankId,
                             String referenceNumber,
                             Double amount,
                             String type,
                             String source,
                             String hostelId) {
}
