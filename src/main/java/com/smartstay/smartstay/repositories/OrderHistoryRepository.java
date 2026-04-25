package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
    OrderHistory findByPaymentLinkId(String paymentLinkId);

    @Query("""
            SELECT oh FROM OrderHistory oh WHERE oh.userType = 'OWNER'
            """)
    List<OrderHistory> findAllRecordByOwner();
}
