package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.ennum.CardType;
import com.smartstay.smartstay.responses.beds.Bank;

import java.util.function.Function;

public class BankingListMapper implements Function<BankingV1, Bank>{

    @Override
    public Bank apply(BankingV1 bankingV1) {

        String cardType = null;
        if (bankingV1.getDebitCardNumber() != null) {
            cardType = CardType.DEBIT.name();
        }
        if (bankingV1.getCreditCardNumber() != null) {
            cardType = CardType.CREDIT.name();
        }
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
                bankingV1.getAccountType(),
                bankingV1.isDefaultAccount(),
                bankingV1.getDescription(),
                cardType,
                 bankingV1.getBalance());
    }
}
