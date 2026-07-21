package com.smartstay.smartstay.payloads.banking;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddBankV2(
        String holderName,
        String bankName,
        String displayName,
        String branchName,
        String accountNo,
        String ifscCode,
        String description,
        Boolean isDefault,
        @NotNull(message = "Account type is required")
        @NotBlank(message = "Account type is required")
        String accountType,
        String bankAccountType,
        Double openingBalance,
        // Mandatory for CASH accounts: sub-type ("Petty Cash" / "Office Cash") and the responsible user id.
        String cashAccountType,
        String responsiblePerson
) {
}
