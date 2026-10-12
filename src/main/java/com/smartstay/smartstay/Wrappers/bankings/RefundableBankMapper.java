package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.responses.invoices.RefundableBanks;

import java.util.function.Function;

public class RefundableBankMapper implements Function<BankingV2, RefundableBanks> {
    @Override
    public RefundableBanks apply(BankingV2 bankingV2) {
        return new RefundableBanks("SSNBB-" + bankingV2.getBankId(),
                bankingV2.getAccountType(),
                bankingV2.getDisplayName());
    }
}
