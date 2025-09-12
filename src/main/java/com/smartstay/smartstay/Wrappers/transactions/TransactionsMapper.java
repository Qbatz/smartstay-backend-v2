package com.smartstay.smartstay.Wrappers.transactions;

import com.smartstay.smartstay.dao.BankTransactionsV1;
import com.smartstay.smartstay.dao.TransactionV1;
import com.smartstay.smartstay.dto.transaction.TransactionDto;
import com.smartstay.smartstay.ennum.BankTransactionType;
import com.smartstay.smartstay.ennum.TransactionType;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class TransactionsMapper implements Function<BankTransactionsV1, TransactionDto> {
    @Override
    public TransactionDto apply(BankTransactionsV1 transactionV1) {
        boolean isCredit = false;
        if (transactionV1.getType().equalsIgnoreCase(BankTransactionType.CREDIT.name())) {
            isCredit = true;
        }
        return new TransactionDto(transactionV1.getTransactionId(),
                transactionV1.getReferenceNumber(),
                transactionV1.getAmount(),
                transactionV1.getType(),
                transactionV1.getSource(),
                transactionV1.getCreatedBy(),
                Utils.dateToString(transactionV1.getCreatedAt()),
                isCredit);
    }
}
