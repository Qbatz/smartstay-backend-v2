package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.PaymentSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentSummaryRepository extends JpaRepository<PaymentSummary, Integer> {
    PaymentSummary findByCustomerId(String customerId);
}
