package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.PaymentSessions;
import com.smartstay.smartstay.ennum.OrderStatus;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.repositories.PaymentSessionsRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PaymentSessionService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private PaymentSessionsRepositories paymentSessionsRepositories;

    public PaymentSessions addPaymentSession(String sessionId, String amount, String hostelId, Double discountAmount, Double planAmount, String planCode) {
        PaymentSessions paymentSessions = new PaymentSessions();
        paymentSessions.setPaymentSessionId(sessionId);
        paymentSessions.setPaymentAmount(Double.parseDouble(amount));
        paymentSessions.setDiscountAmount(discountAmount);
        paymentSessions.setPlanAmount(planAmount);
        paymentSessions.setPlanCode(planCode);
        paymentSessions.setHostelId(hostelId);
        paymentSessions.setCreatedAt(new Date());
        paymentSessions.setCreatedBy(authentication.getName());
        paymentSessions.setPaymentStaus(OrderStatus.CREATED.name());
        return paymentSessionsRepositories.save(paymentSessions);
    }

    public PaymentSessions getPaymentSessionBySessionId(String sessionId) {
        return paymentSessionsRepositories.findByPaymentSessionId(sessionId);
    }
}
