package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.dao.BankingV2;

import java.util.function.Function;

public class BookingBankInfoMapper implements Function<BankingV2, com.smartstay.smartstay.dto.bank.BookingBankInfo> {
    @Override
    public com.smartstay.smartstay.dto.bank.BookingBankInfo apply(BankingV2 bankingV2) {
        return new com.smartstay.smartstay.dto.bank.BookingBankInfo("SSNBB-"+bankingV2.getBankId(),
                bankingV2.getDisplayName(),
                bankingV2.getBankName(),
                null,
                bankingV2.getAccountType(),
                false);
    }
}
