package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.SettlementDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementDetailsRepository extends JpaRepository<SettlementDetails, Long> {
    SettlementDetails findByCustomerId(String customerId);
}
