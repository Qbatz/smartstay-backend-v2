package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.ennum.BankAccountTypeV2;
import com.smartstay.smartstay.responses.invoices.RefundableBanks;

import java.util.function.Function;

public class RefundableBankMapper implements Function<BankingV2, RefundableBanks> {
    @Override
    public RefundableBanks apply(BankingV2 bankingV2) {
        String bankName = bankingV2.getBankName();
        String holderName = bankingV2.getAccountHolderName();
        if (bankingV2.getAccountHolderName() != null) {
            holderName = bankingV2.getDisplayName();
        }

        return new RefundableBanks("SSNBB-" + bankingV2.getBankId(),
                false,
                bankName,
                bankingV2.getAccountType(),
                holderName);
    }
}
