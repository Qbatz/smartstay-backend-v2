package com.smartstay.smartstay.util;

import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.ennum.BankAccountType;

import java.util.List;

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

    public static StringBuilder getBankName(BankingV2 bankingV2, String paymentMethodId) {
        StringBuilder bankName = new StringBuilder();
        if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
            bankName.append(bankingV2.getDisplayName());
            bankName.append("-");
            bankName.append("CASH");
        }
        else if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
            bankName.append(bankingV2.getDisplayName());
            bankName.append("-");
            bankName.append("CARD");
        }
        else if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountType.BANK.name())) {
            List<BankingMethods> bankingMethods = bankingV2
                    .getBankingMethods();
            if (bankingMethods != null) {
                BankingMethods bm = bankingMethods
                        .stream()
                        .filter(i -> i.getPaymentMethodId().equalsIgnoreCase(paymentMethodId))
                        .findFirst()
                        .orElse(null);
                if (bm != null) {
                    bankName.append(bankingV2.getDisplayName());
                    bankName.append("-");
                    bankName.append(bm.getPaymentMethod());
                }
            }
        }

        return bankName;
    }

    public static String getBankName(BankingV2 bankingV2) {
        StringBuilder bankName = new StringBuilder();
        if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
            bankName.append(bankingV2.getDisplayName());
            bankName.append("-");
            bankName.append(bankingV2.getAccountType());
        }
        else if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
            bankName.append(bankingV2.getDisplayName());
            bankName.append("-");
            bankName.append(bankingV2.getAccountType());
        }
        else if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountType.BANK.name())) {
//            bankName.append(bankingV2.getDisplayName());
//            bankName.append("-");
//            bankName.append(bankingV2.getAccountType());
            List<BankingMethods> listBankMethods = bankingV2.getBankingMethods();
            if (listBankMethods != null) {

            }
         }

        return bankName.toString();
    }
}
