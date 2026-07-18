package com.smartstay.smartstay.Wrappers.Banking;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.responses.invoices.BankInfoRecordPayments;
import com.smartstay.smartstay.util.BankUtils;
import com.smartstay.smartstay.util.BankingUtils;

import java.util.function.Function;

public class BankInfoRecordPaymentMapper implements Function<BankingV1, BankInfoRecordPayments> {
    @Override
    public BankInfoRecordPayments apply(BankingV1 bankingV1) {
        return new BankInfoRecordPayments(bankingV1.getBankId(),
                BankingUtils.getPaymentModeWithHolder(bankingV1),
                bankingV1.getAccountHolderName());
    }
}
