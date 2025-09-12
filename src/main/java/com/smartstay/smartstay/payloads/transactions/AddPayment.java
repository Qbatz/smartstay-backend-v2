package com.smartstay.smartstay.payloads.transactions;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AddPayment(
        @Pattern(regexp = "CARD|BANK|UPI|CASH|card|bank|upi|cash", message = "Mode of transaction must be either 'card' or 'upi' or 'bank' or 'cash'")
        String modeOfTransaction,
        String paymentDate,
        String referenceId,
        @NotNull(message = "Amount required")
        Double amount) {
}
