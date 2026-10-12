package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.responses.banking.DebitsBank;

import java.util.function.Function;

public class BankingMethodsMapperV2 implements Function<BankingMethods, DebitsBank> {
    @Override
    public DebitsBank apply(BankingMethods bankingMethods) {
        return new DebitsBank("SSBMB-" + bankingMethods.getPaymentMethodId(),
                bankingMethods.getPaymentMethod(),
                bankingMethods.getDisplayName());
    }
}
