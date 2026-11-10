package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.config.UPITypes;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.ennum.BankAccountTypeV2;
import com.smartstay.smartstay.ennum.CardType;
import com.smartstay.smartstay.ennum.PaymentMethod;
import com.smartstay.smartstay.responses.banking.DebitsBank;

import java.util.function.Function;

public class DebitBanksV2Mapper implements Function<BankingV2, DebitsBank> {

    @Override
    public DebitsBank apply(BankingV2 bankingV2) {
        String accountHolderName = bankingV2.getAccountHolderName();
        String upiAppIcon = null;
        if (bankingV2.getAccountHolderName() == null) {
            accountHolderName = bankingV2.getDisplayName();
        }

        return new DebitsBank("SSNBB-" + bankingV2.getBankId(),
                false,
                bankingV2.getBankName(),
                bankingV2.getAccountType(),
                bankingV2.getDisplayName(),
                accountHolderName,
                null);
    }
}
