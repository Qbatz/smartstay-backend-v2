package com.smartstay.smartstay.responses.banking;

public record PaymentMethodOptionResponse(
        String bankId,
        String accountHolderName,
        String accountNumber,
        String accountType,
        String bankAccountType,
        Double balance,
        String bankName,
        String branchName,
        String displayName,
        String ifscCode,
        String responsiblePersonId,
        String responsiblePerson
) {
}
