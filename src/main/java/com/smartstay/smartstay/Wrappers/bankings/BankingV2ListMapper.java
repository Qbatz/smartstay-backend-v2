package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.ennum.PaymentMethod;
import com.smartstay.smartstay.responses.banking.BankV2List;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class BankingV2ListMapper implements Function<BankingV2, BankV2List> {
    @Override
    public BankV2List apply(BankingV2 bankingV2) {
        String lastTransactionDate = null;
        String cardNumber = null;
        String cardType = null;
        if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
            if (bankingV2.getBankingMethods() != null) {
                BankingMethods bm = bankingV2.getBankingMethods()
                        .stream()
                        .findFirst()
                        .orElse(null);
                if (bm != null) {
                    if (bm.getPaymentMethod().equalsIgnoreCase(PaymentMethod.CREDIT_CARD.name())) {
                        cardType = PaymentMethod.CREDIT_CARD.name();
                        cardNumber = bm.getCardNumber();
                    }
                }
            }

        }
        if (bankingV2.getLastTransaction() != null) {
            lastTransactionDate = Utils.dateToString(bankingV2.getLastTransaction());
        }
        Double lastTransactionAmount = 0.0;
        if (bankingV2.getLastTransactionAmount() != null) {
            lastTransactionAmount = Utils.roundOffWithTwoDigit(bankingV2.getLastTransactionAmount());
        }


        return new BankV2List(bankingV2.getBankId(),
                bankingV2.getAccountType(),
                bankingV2.getBankName(),
                bankingV2.getDisplayName(),
                bankingV2.getAccountNumber(),
                cardNumber,
                cardType,
                bankingV2.getBalance(),
                bankingV2.getAccountHolderName(),
                lastTransactionDate,
                lastTransactionAmount);
    }
}
