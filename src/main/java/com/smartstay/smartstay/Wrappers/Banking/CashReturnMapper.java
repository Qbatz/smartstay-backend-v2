package com.smartstay.smartstay.Wrappers.Banking;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.responses.banking.DebitsBank;

import java.util.function.Function;

public class CashReturnMapper implements Function<BankingV1, DebitsBank> {
    @Override
    public DebitsBank apply(BankingV1 bankingV1) {
        String bankName = null;
        if (bankingV1.getBankName() == null) {
            bankName = bankingV1.getAccountType();
        }
        else {
            bankName = bankingV1.getBankName();
        }
        return new DebitsBank(bankingV1.getBankId(),
                bankName,
                bankingV1.getAccountHolderName());
    }
}
