package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.config.UPITypes;
import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.PaymentMethod;
import com.smartstay.smartstay.responses.invoices.BankInfoRecordPayments;

import java.util.List;
import java.util.function.Function;

public class RecordPaymentBankingMethodMapper implements Function<BankingMethods, BankInfoRecordPayments> {

    private UPITypes upiTypes;

    public RecordPaymentBankingMethodMapper(UPITypes upiTypes) {
        this.upiTypes = upiTypes;
    }

    @Override
    public BankInfoRecordPayments apply(BankingMethods bankingMethods) {
        String accountHolderName = null;
        String bankName = null;
        boolean isUPI = false;
        String upiIcon = null;

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
        if (bankingMethods.getPaymentMethod().equalsIgnoreCase(PaymentMethod.UPI.name())) {
            isUPI = true;
            upiIcon = upiTypes.getUPIType(bankingMethods);
        }
        return new BankInfoRecordPayments("SSBMB-" +bankingMethods.getPaymentMethodId(),
                true,
                bankName,
                bankingMethods.getPaymentMethod(),
                bankingMethods.getDisplayName(),
                accountHolderName,
                isUPI,
                upiIcon);
    }
}
