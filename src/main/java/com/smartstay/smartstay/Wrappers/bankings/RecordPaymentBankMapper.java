package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.responses.invoices.BankInfoRecordPayments;

import java.util.function.Function;

public class RecordPaymentBankMapper implements Function<BankingV2, BankInfoRecordPayments> {
    @Override
    public BankInfoRecordPayments apply(BankingV2 bankingV2) {
        return new BankInfoRecordPayments("SSNBB-" + bankingV2.getBankId(),
                bankingV2.getAccountType(),
                bankingV2.getDisplayName());
    }
}
