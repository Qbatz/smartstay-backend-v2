package com.smartstay.smartstay.Wrappers.assets;

import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.responses.assets.BankForAssets;

import java.util.function.Function;

public class AssetsBankingMethodMapper implements Function<BankingMethods, BankForAssets> {
    @Override
    public BankForAssets apply(BankingMethods bankingMethods) {
        return new BankForAssets("SSBMB-"+ bankingMethods.getPaymentMethodId(),
                bankingMethods.getPaymentMethod(),
                bankingMethods.getDisplayName(),
                null);
    }
}
