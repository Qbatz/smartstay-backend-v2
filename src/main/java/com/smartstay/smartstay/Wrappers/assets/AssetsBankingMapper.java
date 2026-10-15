package com.smartstay.smartstay.Wrappers.assets;

import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.responses.assets.BankForAssets;

import java.util.function.Function;

public class AssetsBankingMapper implements Function<BankingV2, BankForAssets> {
    @Override
    public BankForAssets apply(BankingV2 bankingV2) {

        return new BankForAssets("SSNBB-" + bankingV2.getBankId(),
                bankingV2.getAccountType(),
                bankingV2.getDisplayName(),
                bankingV2.getAccountHolderName());
    }
}
