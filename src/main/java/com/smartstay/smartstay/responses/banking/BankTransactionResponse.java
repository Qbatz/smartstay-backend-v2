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
        String bankAccountType,
        String accountNumber,
        String accountHolderName,
        String bankName,
        String branchName,
        String cashAccountType,
        String displayName,
        String responsiblePerson,
        String responsiblePersonName,
        // ---- banking_methods (null for CASH) ----
        String paymentMethod,
        String cardHolderName,
        String cardNetwork,
        String cardNumber,
        String paymentMethodDisplayName,
        String linkedUpiId,
        String upiApp
) {
}
