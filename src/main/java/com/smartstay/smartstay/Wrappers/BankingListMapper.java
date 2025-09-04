package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.responses.beds.Bank;

import java.util.function.Function;

public class BankingListMapper implements Function<BankingV1, Bank>{

    @Override
    public Bank apply(BankingV1 bankingV1) {

        return new Bank(bankingV1.getBankId(),
                bankingV1.getBankName(),
                bankingV1.getAccountNumber(),
                bankingV1.getIfscCode(),
                bankingV1.getBranchName(),
                bankingV1.getBranchCode(),
                bankingV1.getAccountHolderName(),
                bankingV1.getTransactionType(),
                bankingV1.getUpiId(),
                bankingV1.getCreditCardNumber(),
                bankingV1.getDebitCardNumber(),
                bankingV1.getAccountType());
    }
}
