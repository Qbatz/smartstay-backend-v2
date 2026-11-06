package com.smartstay.smartstay.Wrappers.assets;

import com.smartstay.smartstay.config.UPITypes;
import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.ennum.PaymentMethod;
import com.smartstay.smartstay.responses.assets.BankForAssets;

import java.util.function.Function;

public class AssetsBankingMethodMapper implements Function<BankingMethods, BankForAssets> {
    private UPITypes upiTypes = null;

    public AssetsBankingMethodMapper(UPITypes upiTypes) {
        this.upiTypes = upiTypes;
    }

    @Override
    public BankForAssets apply(BankingMethods bankingMethods) {

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
        return new BankForAssets("SSBMB-"+ bankingMethods.getPaymentMethodId(),
                true,
                bankName,
                bankingMethods.getPaymentMethod(),
                bankingMethods.getDisplayName(),
                accountHolderName,
                isUPI,
                upiIcon);
    }
}
