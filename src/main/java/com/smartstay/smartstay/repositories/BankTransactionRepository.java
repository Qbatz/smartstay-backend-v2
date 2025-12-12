package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BankTransactionsV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransactionsV1, Integer>  {

    List<BankTransactionsV1> findByBankIdIn(List<String> listBankIds);

    List<BankTransactionsV1> findByHostelId(String hostelId);

    BankTransactionsV1 findTopByBankIdOrderByTransactionDateDesc(String bankId);

    BankTransactionsV1 findByBankIdAndHostelId(String bankId, String hostelId);

    BankTransactionsV1 findTopByBankIdAndHostelIdOrderByCreatedAtDesc(String bankId, String hostelId);

    BankTransactionsV1 findByTransactionNumber(String transactionNumber);



}
