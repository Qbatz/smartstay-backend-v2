package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BankTransactionsV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransactionsV1, Integer>  {

    List<BankTransactionsV1> findByBankIdIn(List<String> listBankIds);

    BankTransactionsV1 findTopByBankIdOrderByTransactionDateDesc(String bankId);
}
