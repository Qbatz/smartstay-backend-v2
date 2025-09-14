package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.transactions.TransactionsMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.BankTransactionsV1;
import com.smartstay.smartstay.dto.bank.TransactionDto;
import com.smartstay.smartstay.ennum.BankTransactionType;
import com.smartstay.smartstay.ennum.TransactionType;
import com.smartstay.smartstay.repositories.BankTransactionRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class BankTransactionService {

    @Autowired
    private Authentication authentication;

    @Autowired
    private UsersService usersService;

    @Autowired
    private BankTransactionRepository bankRepository;


    public int addTransaction(TransactionDto transactionDto) {
        if (authentication.isAuthenticated()) {
            BankTransactionsV1 transactionsV1 = new BankTransactionsV1();
            BankTransactionsV1 v1 = bankRepository.findTopByBankIdOrderByTransactionDateDesc(transactionDto.bankId());
            if (v1 == null) {
                if (transactionDto.type().equalsIgnoreCase(BankTransactionType.DEBIT.name())) {
                    transactionsV1.setAccountBalance(-transactionDto.amount());
                }
                else {
                    transactionsV1.setAccountBalance(transactionDto.amount());
                }
            }
            else {
                if (transactionDto.type().equalsIgnoreCase(BankTransactionType.DEBIT.name())) {
                    transactionsV1.setAccountBalance(v1.getAccountBalance() - transactionDto.amount());
                }
                else {
                    Double accountBalance = 0.0;
                    if (v1.getAccountBalance() != null) {
                        accountBalance = v1.getAccountBalance();
                    }
                    transactionsV1.setAccountBalance(transactionDto.amount() + accountBalance);
                }
            }
            transactionsV1.setTransactionDate(Utils.stringToDate(transactionDto.transactionDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));
            transactionsV1.setBankId(transactionDto.bankId());
            transactionsV1.setReferenceNumber(transactionDto.referenceNumber());
            transactionsV1.setAmount(transactionDto.amount());
            transactionsV1.setType(transactionDto.type());
            transactionsV1.setSource(transactionDto.source());
            transactionsV1.setHostelId(transactionDto.hostelId());
            transactionsV1.setCreatedAt(new Date());
            transactionsV1.setCreatedBy(authentication.getName());

            bankRepository.save(transactionsV1);
        }

        return 1;
    }


    public List<com.smartstay.smartstay.dto.transaction.TransactionDto> getAllTransactions(List<String> listBankIds) {
        return bankRepository.findByBankIdIn(listBankIds)
                .stream()
                .map(item -> new TransactionsMapper().apply(item))
                .toList();
    }
}
