package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.TempTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TempTransactionsRepositories extends JpaRepository<TempTransactions, Integer> {
}
