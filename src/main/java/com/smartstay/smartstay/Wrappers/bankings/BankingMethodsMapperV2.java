package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.config.UPITypes;
import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.ennum.PaymentMethod;
import com.smartstay.smartstay.responses.banking.DebitsBank;

import java.util.function.Function;

public class BankingMethodsMapperV2 implements Function<BankingMethods, DebitsBank> {
    private UPITypes upiTypes;

    public BankingMethodsMapperV2(UPITypes upiTypes) {
        this.upiTypes = upiTypes;
    }

    @Override
    public DebitsBank apply(BankingMethods bankingMethods) {
        String upiIcons = null;
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

        if (bankingMethods.getPaymentMethod().equalsIgnoreCase(PaymentMethod.UPI.name())) {
            bankName = bankingMethods.getUpiApp();
            upiIcons = upiTypes.getUPIType(bankingMethods);
        }
        return new DebitsBank("SSBMB-" + bankingMethods.getPaymentMethodId(),
                true,
                bankName,
                bankingMethods.getPaymentMethod(),
                bankingMethods.getDisplayName(),
                accountHolderName,
                upiIcons);
    }
}
