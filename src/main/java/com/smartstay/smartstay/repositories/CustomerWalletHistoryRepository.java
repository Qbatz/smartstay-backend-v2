package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.CustomerWalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerWalletHistoryRepository extends JpaRepository<CustomerWalletHistory, Long> {
    List<CustomerWalletHistory> findByCustomerIdIn(List<String> customerId);
    List<CustomerWalletHistory> findByCustomerId(String customerId);
    @Query("""
            SELECT cwh FROM CustomerWalletHistory cwh WHERE cwh.customerId=:customerId AND 
            cwh.billingStatus='INVOICE_NOT_GENERATED'
            """)
    List<CustomerWalletHistory> findInvoiceNotGeneratedByCustomerId(String customerId);
}
