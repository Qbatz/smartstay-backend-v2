package com.smartstay.smartstay.responses.banking;

public record BankTransactionResponse(
        // ---- bank_transactionsv1 ----
        String createdAt,
        Double transactionAmount,
        Integer transactionId,
        Double accountBalance,
        String bankId,
        String createdBy,
        String createdByName,
        String description,
        String referenceNumber,
        String source,
        String transactionNumber,
        String type,
        String sourceId,
        String investorName,
        // ---- bankingv2 ----
        String accountType,
        String bankAccountType,
        String accountHolderName,
        String bankName,
        String cashAccountType,
        String displayName,
        String paymentMethod
) {
}
