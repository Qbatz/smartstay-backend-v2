package com.smartstay.smartstay.payloads.banking;

import jakarta.validation.constraints.NotBlank;

public record SelfTransfer(
        @NotBlank(message = "fromBankId cannot be null or blank")
        String fromBankId,
        @NotBlank(message = "toBankId cannot be null or blank")
        String toBankId,
        Double balance
) {
}