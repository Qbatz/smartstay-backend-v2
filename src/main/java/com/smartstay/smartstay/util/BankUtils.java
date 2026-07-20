package com.smartstay.smartstay.util;

import com.smartstay.smartstay.dao.BankingV1;

public class BankUtils {
    public static String getBankName(BankingV1 bankingV1) {
        String bankName = null;
        if (bankingV1.getBankName() != null && !bankingV1.getBankName().isBlank()) {
            bankName = bankingV1.getBankName();
        }
        else {
            bankName = bankingV1.getAccountType();
        }

        return bankName;
    }
}
