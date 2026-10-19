package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.ennum.PaymentMethod;
import com.smartstay.smartstay.responses.banking.CreditCardInitialization;

import java.util.List;
import java.util.function.Function;

public class BankingV2CreditCardInitializationMapper implements Function<BankingV2, CreditCardInitialization> {
    @Override
    public CreditCardInitialization apply(BankingV2 bankingV2) {
        String cardNetwork = null;
        Double balance = 0.0;
        if (bankingV2.getBalance() != null) {
            balance = bankingV2.getBalance();
        }
        if (bankingV2.getBankingMethods() != null) {
            BankingMethods bm = bankingV2
                    .getBankingMethods()
                    .stream()
                    .filter(i -> i.getPaymentMethod().equalsIgnoreCase(PaymentMethod.CREDIT_CARD.name()))
                    .findFirst()
                    .orElse(null);
            if (bm != null) {
                cardNetwork = bm.getCardNetwork();
            }

        }
        return new CreditCardInitialization(bankingV2.getBankId(),
                bankingV2.getCardNumber(),
                cardNetwork,
                balance,
                bankingV2.getBankName());
    }
}
