package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.PaymentMethod;
import com.smartstay.smartstay.responses.invoices.BankInfoRecordPayments;

import java.util.List;
import java.util.function.Function;

public class RecordPaymentBankingMethodMapper implements Function<BankingMethods, BankInfoRecordPayments> {

    @Override
    public BankInfoRecordPayments apply(BankingMethods bankingMethods) {
        String accountHolderName = null;
        String bankName = null;
        BankingV2 bankingV2 = bankingMethods.getBank();
        if (bankingV2 != null) {
            bankName = bankingV2.getBankName();
            if (bankingV2.getAccountHolderName() != null) {
                accountHolderName = bankingV2.getAccountHolderName();
            }
            else {
                accountHolderName = bankingV2.getDisplayName();
            }
        }
        return new BankInfoRecordPayments("SSBMB-" +bankingMethods.getPaymentMethodId(),
                true,
                bankName,
                bankingMethods.getPaymentMethod(),
                bankingMethods.getDisplayName(),
                accountHolderName);
    }
}
