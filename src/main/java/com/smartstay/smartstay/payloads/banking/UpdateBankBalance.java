package com.smartstay.smartstay.payloads.banking;

import jakarta.validation.constraints.NotBlank;

public record UpdateBankBalance(
        @NotBlank(message = "bankId cannot be null or blank")
        String bankId,
        Double balance
) {
}
