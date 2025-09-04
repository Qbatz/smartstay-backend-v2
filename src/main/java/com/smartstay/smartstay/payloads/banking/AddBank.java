package com.smartstay.smartstay.payloads.banking;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AddBank(
        @NotNull(message = "Holder name required")
        @NotEmpty(message = "Holder name required")
        String holderName,
        String bankName,
        @Pattern(regexp = "^(\\\\d{9,18})?$", message = "Account number must be 9–18 digits")
        String accountNo,
        String ifscCode,
        String description,
        Boolean isDefault,
        String upiId,
        @Pattern(regexp = "^(CREDIT|DEBIT|credit|debit)?$", message = "Card type must be either 'credit' or 'debit'")
        String cardType,
        @NotNull(message = "Account type required")
        @NotEmpty(message = "Account type required")
        @Pattern(regexp = "upi|card|cash|bank|UPI|CARD|CASH|BANK", message = "Type must be either 'upi' or 'card' or 'cash' or 'bank'")
        String accountType,
        String cardNumber) {
}
