package com.smartstay.smartstay.dto.bank;

public record LinkedBankAccounts(String linkedBankId,
                                 String bankName,
                                 String displayName,
                                 String accountNumber) {
}
