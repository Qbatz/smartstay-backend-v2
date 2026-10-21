package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.responses.banking.PaymentMethodOptionResponse;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class BankingV2PaymentMethodResponse implements Function<BankingV2, PaymentMethodOptionResponse> {
    @Override
    public PaymentMethodOptionResponse apply(BankingV2 bankingV2) {
        String bankAccountType = null;
        if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
            bankAccountType = bankingV2.getCashAccountType();
        }
        else if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountType.BANK.name())) {
            bankAccountType = bankingV2.getBankAccountType();
        }
        else {
            bankAccountType = bankingV2.getBankAccountType();
        }
        return new PaymentMethodOptionResponse(bankingV2.getBankId(),
                bankingV2.getAccountHolderName(),
                bankingV2.getAccountNumber(),
                bankingV2.getAccountType(),
                bankAccountType,
                Utils.roundOffWithTwoDigit(bankingV2.getBalance()),
                bankingV2.getBankName(),
                bankingV2.getBranchName(),
                bankingV2.getDisplayName(),
                bankingV2.getIfscCode(),
                bankingV2.getResponsiblePerson(),
                "");
    }
}
