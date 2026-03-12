package com.smartstay.smartstay.util;


import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.ennum.BankAccountType;

public class BankingUtils {
    public static String getPaymentModeWithHolder(BankingV1 bankingV1) {
        StringBuilder bank = new StringBuilder();
        if (bankingV1 != null) {
            bank.append(bankingV1.getAccountHolderName());
            if (bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
                bank.append("-");
                bank.append("Cash");
            }
            else if(bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
                if (bankingV1.getAccountHolderName() != null) {

                    bank.append("-");
                    bank.append("Card");
                }
            }
            else if(bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.BANK.name())) {
                if (bankingV1.getAccountHolderName() != null) {
                    bank.append("-");
                    bank.append(bankingV1.getBankName());
                }
            }
            else if(bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.UPI.name())) {
                if (bankingV1.getAccountHolderName() != null) {
                    bank.append("-");
                    bank.append("Upi");
                }
            }
        }

        return bank.toString();
    }
}
