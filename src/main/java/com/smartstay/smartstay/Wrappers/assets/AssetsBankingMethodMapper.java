package com.smartstay.smartstay.Wrappers.assets;

import com.smartstay.smartstay.config.UPITypes;
import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.ennum.PaymentMethod;
import com.smartstay.smartstay.responses.assets.BankForAssets;

import java.util.function.Function;

public class AssetsBankingMethodMapper implements Function<BankingMethods, BankForAssets> {
    private final UPITypes upiTypes;

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
            accountHolderName = bankingV2.getAccountHolderName() != null
                    ? bankingV2.getAccountHolderName()
                    : bankingV2.getDisplayName();
        }
        if (PaymentMethod.UPI.name().equalsIgnoreCase(bankingMethods.getPaymentMethod())) {
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
