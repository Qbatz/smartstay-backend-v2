package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.responses.invoices.RefundableBanks;

import java.util.function.Function;

public class RefundableBankingMethodsMapper implements Function<BankingMethods, RefundableBanks> {
    @Override
    public RefundableBanks apply(BankingMethods bankingMethods) {
        String bankName = null;
        BankingV2 bankingV2 = bankingMethods.getBank();
        if (bankingV2 != null) {
            bankName = bankingV2.getBankName();
        }

        return new RefundableBanks("SSBMB-" + bankingMethods.getPaymentMethodId(),
                true,
                bankName,
                bankingMethods.getPaymentMethod(),
                bankingMethods.getDisplayName());
    }
}
