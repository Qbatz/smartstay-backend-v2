package com.smartstay.smartstay.responses.banking;

public record BankV2List(String bankId,
                         String accountType,
                         String cashAccountType,
                         String bankName,
                         String displayName,
                         String accountNumber,
                         String cardNumber,
                         String branchName,
                         String cardType,
                         Double balance,
                         String accountHolder,
                         String bankAccountType,
                         String ifscCode,
                         String responsiblePerson,
                         Double creditLimit,
                         String linkedBank,
                         String cardNerwork,
                         String lastTransactionDate,
                         Double lastTransactionAmount,
                         boolean isDeleted) {
}
