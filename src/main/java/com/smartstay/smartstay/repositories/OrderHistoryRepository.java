package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
    OrderHistory findByPaymentLinkId(String paymentLinkId);
}
