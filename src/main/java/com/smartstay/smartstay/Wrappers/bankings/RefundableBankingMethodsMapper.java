package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.responses.invoices.RefundableBanks;

import java.util.function.Function;

public class RefundableBankingMethodsMapper implements Function<BankingMethods, RefundableBanks> {
    @Override
    public RefundableBanks apply(BankingMethods bankingMethods) {

        return new RefundableBanks("SSBMB-" + bankingMethods.getPaymentMethodId(),
                bankingMethods.getPaymentMethod(),
                bankingMethods.getDisplayName());
    }
}
