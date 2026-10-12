package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.responses.invoices.BankInfoRecordPayments;

import java.util.function.Function;

public class RecordPaymentBankingMethodMapper implements Function<BankingMethods, BankInfoRecordPayments> {
    @Override
    public BankInfoRecordPayments apply(BankingMethods bankingMethods) {
        return new BankInfoRecordPayments("SSBMB-" +bankingMethods.getPaymentMethodId(),
                bankingMethods.getPaymentMethod(),
                bankingMethods.getDisplayName());
    }
}
