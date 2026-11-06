package com.smartstay.smartstay.Wrappers.assets;

import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.responses.assets.BankForAssets;

import java.util.function.Function;

public class AssetsBankingMapper implements Function<BankingV2, BankForAssets> {
    @Override
    public BankForAssets apply(BankingV2 bankingV2) {

        String accountHolderName = bankingV2.getAccountHolderName();
        if (bankingV2.getAccountHolderName() == null) {
            accountHolderName = bankingV2.getDisplayName();
        }
        return new BankForAssets("SSNBB-" + bankingV2.getBankId(),
                false,
                bankingV2.getBankName(),
                bankingV2.getAccountType(),
                bankingV2.getDisplayName(),
                accountHolderName,
                false,
                null);
    }
}
