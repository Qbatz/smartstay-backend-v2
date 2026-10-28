package com.smartstay.smartstay.Wrappers.invoices;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.responses.invoices.RefundableBanks;

import java.util.function.Function;

public class RefundableBanksMapper implements Function<BankingV1, RefundableBanks> {
    @Override
    public RefundableBanks apply(BankingV1 bankingV2) {

        String bankName = bankingV2.getBankName();
        String holderName = bankingV2.getAccountHolderName();
        if (bankingV2.getAccountHolderName() != null) {
            holderName = bankingV2.getAccountHolderName();
        }

        return new RefundableBanks("SSNBB-" + bankingV2.getBankId(),
                false,
                bankName,
                bankingV2.getAccountType(),
                holderName);
    }
}
