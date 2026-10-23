package com.smartstay.smartstay.util;


import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.ennum.PaymentMethod;
import com.smartstay.smartstay.responses.invoices.AccountDetails;

import java.util.List;

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

    public static AccountDetails getPaymentModeWithName(BankingV2 bankingV2, String paymentMethodId) {
        if (bankingV2 != null) {
            if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
                return new AccountDetails(bankingV2.getAccountNumber(), bankingV2.getIfscCode(), bankingV2.getDisplayName() + "-" + bankingV2.getAccountType(), null, null);
            }
            else if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
                return new AccountDetails(bankingV2.getAccountNumber(), bankingV2.getIfscCode(), bankingV2.getDisplayName() + "-" + bankingV2.getAccountType(), null, null);
            }
            else {
                List<BankingMethods> listBankingMethods = bankingV2
                        .getBankingMethods();
                if (listBankingMethods != null) {
                    BankingMethods bm = listBankingMethods
                            .stream()
                            .filter(i -> i.getPaymentMethodId().equalsIgnoreCase(paymentMethodId))
                            .findFirst()
                            .orElse(null);
                    if (bm != null) {
                        return new AccountDetails(bankingV2.getAccountNumber(), bankingV2.getIfscCode(), bankingV2.getBankName(), bm.getUpiId(), bm.getQrImage());
                    }
                }
                return null;
            }
        }

        return null;

    }

    public static String getBankName(BankingV2 bankingV2, String paymentMethodId) {
        if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
            StringBuilder builder = new StringBuilder();
            builder.append(bankingV2.getDisplayName());
            builder.append("-");
            builder.append(bankingV2.getBankAccountType());
            return builder.toString();
        }
        else if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
            StringBuilder builder = new StringBuilder();
            builder.append(bankingV2.getDisplayName());
            builder.append("-");
            builder.append(bankingV2.getBankAccountType());
            return builder.toString();
        }
        else {
            List<BankingMethods> bankingMethods = bankingV2.getBankingMethods();
            if (bankingMethods != null) {
                BankingMethods bm = bankingMethods
                        .stream()
                        .filter(i -> i.getPaymentMethodId().equalsIgnoreCase(paymentMethodId))
                        .findFirst()
                        .orElse(null);
                if (bm != null) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(bm.getPaymentMethod());
                    builder.append("-");
                    builder.append(bm.getDisplayName());
                    return builder.toString();
                }
            }
            return null;
        }
    }

    public static String getPaymentMethod(BankingV2 bankingV2, String paymentMethod) {
        if (bankingV2 != null) {
            List<BankingMethods> listBankMethods = bankingV2
                    .getBankingMethods();
            if (listBankMethods != null) {
                BankingMethods bm = listBankMethods
                        .stream()
                        .filter(i -> i.getPaymentMethodId().equalsIgnoreCase(paymentMethod))
                        .findFirst()
                        .orElse(null);

                if (bm != null) {
                    return bm.getPaymentMethod();
                }
            }
        }

        return null;
    }

    public static String getPaymentMethodName(BankingMethods bm) {
        if (bm != null) {
            if (bm.getPaymentMethod().equalsIgnoreCase(PaymentMethod.BANK.name())) {
                return bm.getDisplayName();
            }
            if (bm.getPaymentMethod().equalsIgnoreCase(PaymentMethod.UPI.name())) {
                return bm.getUpiApp();
            }
            if (bm.getPaymentMethod().equalsIgnoreCase(PaymentMethod.DEBIT_CARD.name())) {
                return bm.getPaymentMethod();
            }
            if (bm.getPaymentMethod().equalsIgnoreCase(PaymentMethod.CREDIT_CARD.name())) {
                return bm.getPaymentMethod();
            }
        }

        return "";
    }
}
