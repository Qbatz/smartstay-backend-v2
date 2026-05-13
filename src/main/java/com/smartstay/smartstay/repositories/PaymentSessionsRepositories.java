package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.PaymentSessions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentSessionsRepositories extends JpaRepository<PaymentSessions, Long> {
    PaymentSessions findByPaymentSessionId(String sessionId);
}
