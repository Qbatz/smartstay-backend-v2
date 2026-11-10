package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.config.UPITypes;
import com.smartstay.smartstay.dao.BankingV2;

import java.util.function.Function;

public class BookingBankInfoMapper implements Function<BankingV2, com.smartstay.smartstay.dto.bank.BookingBankInfo> {

    @Override
    public com.smartstay.smartstay.dto.bank.BookingBankInfo apply(BankingV2 bankingV2) {
        String accountHolderName = bankingV2.getAccountHolderName();
        if (bankingV2.getAccountHolderName() == null) {
            accountHolderName = bankingV2.getDisplayName();
        }
        return new com.smartstay.smartstay.dto.bank.BookingBankInfo("SSNBB-"+bankingV2.getBankId(),
                false,
                bankingV2.getBankName(),
                bankingV2.getAccountType(),
                bankingV2.getDisplayName(),
               accountHolderName,
                false,
                null);
    }
}
