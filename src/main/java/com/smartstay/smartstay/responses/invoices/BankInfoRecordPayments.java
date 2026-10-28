package com.smartstay.smartstay.responses.invoices;

public record BankInfoRecordPayments(String bankId,
                                     boolean isBank,
                                     String bankName,
                                     String accountType,
                                     String displayName,
                                     String accountHolderName) {
}
