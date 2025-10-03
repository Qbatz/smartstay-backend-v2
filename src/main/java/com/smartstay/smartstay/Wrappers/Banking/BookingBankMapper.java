package com.smartstay.smartstay.Wrappers.Banking;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dto.bank.BookingBankInfo;
import com.smartstay.smartstay.ennum.BankAccountType;

import java.util.function.Function;

public class BookingBankMapper implements Function<BankingV1, BookingBankInfo> {
    @Override
    public BookingBankInfo apply(BankingV1 bankingV1) {

        boolean isUpi = false;
        if (bankingV1.getUpiId() != null && bankingV1.getAccountNumber() == null) {
            isUpi = true;
        }
        String bankName = bankingV1.getBankName();
        if (bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
            bankName = BankAccountType.CASH.name();
        }
        return new BookingBankInfo(
                bankingV1.getBankId(),
                bankingV1.getAccountHolderName(),
                bankName,
                bankingV1.getUpiId(),
                bankingV1.getAccountType(),
                isUpi
        );
    }
}
