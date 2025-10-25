package com.smartstay.smartstay.Wrappers.invoices;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.responses.invoices.RefundableBanks;

import java.util.function.Function;

public class RefundableBanksMapper implements Function<BankingV1, RefundableBanks> {
    @Override
    public RefundableBanks apply(BankingV1 bankingV1) {
        StringBuilder bankName = new StringBuilder();
        if (bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name()) ||
                bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name()) ||
                bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.UPI.name())) {
            bankName.append(bankingV1.getAccountHolderName());
            bankName.append("-");
            bankName.append(bankingV1.getAccountType());
        }

        return new RefundableBanks(bankingV1.getBankId(),
                bankName.toString(),
                bankingV1.getAccountHolderName());
    }
}
