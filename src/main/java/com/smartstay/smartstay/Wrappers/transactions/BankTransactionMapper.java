package com.smartstay.smartstay.Wrappers.transactions;

import com.smartstay.smartstay.dto.bank.TransactionDto;
import com.smartstay.smartstay.responses.banking.BankList;

import java.util.function.Function;

public class BankTransactionMapper implements Function<TransactionDto, BankList> {
    @Override
    public BankList apply(TransactionDto transactionDto) {
        return null;
    }
}
