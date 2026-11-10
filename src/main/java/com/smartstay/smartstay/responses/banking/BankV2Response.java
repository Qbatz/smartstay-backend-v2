package com.smartstay.smartstay.responses.banking;

public record BankV2Response(
        String bankId,
        String displayName,
        String bankName,
        String accountNumber,
        String accountHolderName,
        String accountType,
        String bankAccountType,
        String cashAccountType,
        String responsiblePerson,
        String branchName,
        Double balance,
        boolean isActive,
        boolean isDefaultAccount
) {
}
