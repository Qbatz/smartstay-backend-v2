package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.responses.banking.DebitsBank;

import java.util.function.Function;

public class DebitBanksV2Mapper implements Function<BankingV2, DebitsBank> {
    @Override
    public DebitsBank apply(BankingV2 bankingV2) {

        return new DebitsBank("SSNBB-" + bankingV2.getBankId(),
                bankingV2.getAccountType(),
                bankingV2.getDisplayName());
    }
}
