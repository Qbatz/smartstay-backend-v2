package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.SettlementItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementItemsRepository extends JpaRepository<SettlementItems, Long> {
    SettlementItems findByInvoiceId(String invoiceId);
}
