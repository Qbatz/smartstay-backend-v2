package com.smartstay.smartstay.payloads.banking;

import jakarta.validation.constraints.Pattern;

public record UpdateBankPayload(String accountHolderName,
                                String bankName,
                                String displayName,
                                String branchName,
                                String accountNo,
                                String ifscCode,
                                String description,
                                @Pattern(regexp = "CARD|card|Card|CASH|cash|Cash|BANK|bank|Bank", message = "source must be either 'CARD' or 'card' or 'CASH' or 'Cash' or 'BANK' or 'Bank")
                                String newAccountType,
                                String cashAccountType,
                                String responsiblePerson,
                                String cardNumber,
                                String creditLimit,
                                String billingCycle,
                                String cardNetwork) {
}
