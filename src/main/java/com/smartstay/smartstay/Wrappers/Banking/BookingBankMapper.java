package com.smartstay.smartstay.Wrappers.Banking;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dto.bank.BookingBankInfo;

import java.util.function.Function;

public class BookingBankMapper implements Function<BankingV1, BookingBankInfo> {
    @Override
    public BookingBankInfo apply(BankingV1 bankingV1) {

        boolean isUpi = false;
        if (bankingV1.getUpiId() != null && bankingV1.getAccountNumber() == null) {
            isUpi = true;
        }
        return new BookingBankInfo(
                bankingV1.getBankId(),
                bankingV1.getAccountHolderName(),
                bankingV1.getBankName(),
                bankingV1.getUpiId(),
                bankingV1.getAccountType(),
                isUpi
        );
    }
}
