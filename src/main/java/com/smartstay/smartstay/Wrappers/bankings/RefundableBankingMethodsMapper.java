package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.config.UPITypes;
import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.ennum.PaymentMethod;
import com.smartstay.smartstay.responses.invoices.RefundableBanks;

import java.util.function.Function;

public class RefundableBankingMethodsMapper implements Function<BankingMethods, RefundableBanks> {
    private final UPITypes upiTypes;

    public RefundableBankingMethodsMapper(UPITypes upiTypes) {
        this.upiTypes = upiTypes;
    }

    @Override
    public RefundableBanks apply(BankingMethods bankingMethods) {
        String bankName = null;
        boolean isUPI = false;
        String upiIcon = null;
        String accountHolderName = null;
        BankingV2 bankingV2 = bankingMethods.getBank();
        if (bankingV2 != null) {
            bankName = bankingV2.getBankName();
            accountHolderName = bankingV2.getAccountHolderName();
        }

        if (PaymentMethod.UPI.name().equalsIgnoreCase(bankingMethods.getPaymentMethod())) {
            isUPI = true;
            upiIcon = upiTypes.getUPIType(bankingMethods);
        }

        return new RefundableBanks("SSBMB-" + bankingMethods.getPaymentMethodId(),
                true,
                bankName,
                bankingMethods.getPaymentMethod(),
                accountHolderName,
                bankingMethods.getDisplayName(),
                isUPI,
                upiIcon);
    }
}
