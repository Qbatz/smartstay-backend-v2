package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.config.UPITypes;
import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.dto.bank.BookingBankInfo;
import com.smartstay.smartstay.ennum.PaymentMethod;

import java.util.function.Function;

public class BookingBankMethodsMapper implements Function<BankingMethods, BookingBankInfo> {

    private UPITypes upiTypes;

    public BookingBankMethodsMapper(UPITypes upiTypes) {
        this.upiTypes = upiTypes;
    }

    @Override
    public BookingBankInfo apply(BankingMethods bankingMethods) {
        boolean isUPI = false;
        String upiIcon = upiTypes.getUPIType(bankingMethods);
        if (bankingMethods.getPaymentMethod().equalsIgnoreCase(PaymentMethod.UPI.name())) {
            isUPI = true;
        }

        String accountHolderName = null;
        String bankName = null;
        BankingV2 bankingV2 = bankingMethods.getBank();
        if (bankingV2 != null) {
            bankName = bankingV2.getBankName();
            if (bankingV2.getAccountHolderName() != null) {
                accountHolderName = bankingV2.getAccountHolderName();
            }
            else {
                accountHolderName = bankingV2.getDisplayName();
            }
        }

        return new BookingBankInfo("SSBMB-" + bankingMethods.getPaymentMethodId(),
                true,
                bankName,
                bankingMethods.getPaymentMethod(),
                bankingMethods.getDisplayName(),
                accountHolderName,
                isUPI,
                upiIcon);
    }
}
