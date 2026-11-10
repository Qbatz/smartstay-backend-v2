package com.smartstay.smartstay.responses.banking;

import com.smartstay.smartstay.dto.bank.LinkedBankAccounts;

public record CreditCardInitialization(String cardId,
                                       String cardNumber,
                                       String cardNetwork,
                                       Double payableAmount,
                                       String bankName,
                                       boolean isUpi,
                                       String upiAppIcon,
                                       LinkedBankAccounts linkedBankAccount) {
}
