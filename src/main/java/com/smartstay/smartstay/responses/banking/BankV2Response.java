package com.smartstay.smartstay.responses.banking;

public record BankV2Response(
        String bankId,
        String displayName,
        String bankName,
        String accountNumber,
        String ifscCode,
        String branchName,
        String accountHolderName,
        String accountType,
        String bankAccountType,
        String description,
        Double balance,
        boolean isActive,
        boolean isDefaultAccount,
        String hostelId,
        String platform,
        String createdBy,
        String createdAt
) {
}
