package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dto.bank.BookingBankInfo;
import com.smartstay.smartstay.ennum.PaymentMethod;

import java.util.function.Function;

public class BookingBankMethodsMapper implements Function<BankingMethods, BookingBankInfo> {
    @Override
    public BookingBankInfo apply(BankingMethods bankingMethods) {
        boolean isUPI = false;
        if (bankingMethods.getPaymentMethod().equalsIgnoreCase(PaymentMethod.UPI.name())) {
            isUPI = true;
        }

        return new BookingBankInfo("SSBMB-" + bankingMethods.getPaymentMethodId(),
                bankingMethods.getDisplayName(),
                "",
                bankingMethods.getUpiId(),
                bankingMethods.getPaymentMethod(),
                isUPI);
    }
}
