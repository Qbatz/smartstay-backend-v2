package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.TenantBankTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantBankTransactionRepositories extends JpaRepository<TenantBankTransactions, Long> {
}
