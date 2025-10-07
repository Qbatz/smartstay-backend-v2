package com.smartstay.smartstay.Wrappers.Banking;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.responses.bookings.CashReturnBank;

import java.util.function.Function;

public class CashReturnMapper implements Function<BankingV1, CashReturnBank> {
    @Override
    public CashReturnBank apply(BankingV1 bankingV1) {
        return new CashReturnBank(bankingV1.getBankId(),
                bankingV1.getBankName(),
                bankingV1.getAccountHolderName());
    }
}
