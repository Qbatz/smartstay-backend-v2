package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomerWallerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerWalletHistoryRepository extends JpaRepository<CustomerWallerHistory, Long> {
}
