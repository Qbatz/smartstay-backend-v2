package com.smartstay.smartstay.payloads.banking;

import jakarta.validation.constraints.Pattern;

public record UpdateBankPayload(String accountHolderName,
                                String bankName,
                                String displayName,
                                String branchName,
                                String accountNo,
                                String ifscCode,
                                String description,
                                @Pattern(regexp = "CARD|card|Card|CASH|cash|Cash|BANK|bank|Bank", message = "account type must be either 'CARD' or 'card' or 'CASH' or 'Cash' or 'BANK' or 'Bank")
                                String newAccountType,
                                @Pattern(regexp = "CURRENT|current|SAVINGS|savings", message = "bank account type must be either 'CURRENT' or 'current' or 'SAVINGS' or 'savings'")
                                String bankAccountType,
                                String cashAccountType,
                                String responsiblePerson,
                                String cardNumber,
                                String creditLimit,
                                String billingCycle,
                                String linkedBankId,
                                String cardNetwork) {
}
