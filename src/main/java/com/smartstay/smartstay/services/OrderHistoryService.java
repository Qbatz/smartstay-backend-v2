package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.OrderHistory;
import com.smartstay.smartstay.dao.PaymentSessions;
import com.smartstay.smartstay.ennum.OrderStatus;
import com.smartstay.smartstay.ennum.UserType;
import com.smartstay.smartstay.events.HostelEvents;
import com.smartstay.smartstay.events.SubscriptionEvents;
import com.smartstay.smartstay.payloads.subscription.PaymentLinks;
import com.smartstay.smartstay.payloads.subscription.PaymentStatus;
import com.smartstay.smartstay.payloads.subscription.PaymentStatusCardType;
import com.smartstay.smartstay.payloads.subscription.ZohoPaymentResponse;
import com.smartstay.smartstay.repositories.OrderHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderHistoryService {
    @Autowired
    private OrderHistoryRepository orderHistoryRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private PaymentSessionService paymentSessionService;

    public void createOrder(String hostelId, PaymentLinks details, Double finalAmount, String planCode, Double discountAmount, Double planPrice) {
        OrderHistory orderHistory = new OrderHistory();
        orderHistory.setCreatedBy(authentication.getName());
        orderHistory.setCreatedAt(new Date());
        orderHistory.setPlanAmount(planPrice);
        orderHistory.setPlanCode(planCode);
        orderHistory.setTotalAmount(finalAmount);
        orderHistory.setDiscountAmount(discountAmount);
        orderHistory.setOrderStatus(OrderStatus.CREATED.name());
        orderHistory.setActive(true);
        orderHistory.setHostelId(hostelId);
        orderHistory.setPaymentUrl(details.paymentLink());
        orderHistory.setPaymentLinkId(details.paymentLinkId());
        orderHistory.setUserType(UserType.OWNER.name());
        orderHistory.setPaidBy(authentication.getName());


        orderHistoryRepository.save(orderHistory);
    }

    public void successfullPayment(Object payload) {
        ZohoPaymentResponse paymentLinks = (ZohoPaymentResponse) payload;
        OrderHistory orderHistory = orderHistoryRepository.findByPaymentLinkId(paymentLinks.linkId());
        orderHistory.setPaymentType(paymentLinks.type());
        if (paymentLinks.type().equalsIgnoreCase("UPI")) {
            PaymentStatus status = paymentLinks.upiStatus();
            orderHistory.setChannel(status.channel());
            orderHistory.setUpiId(status.id());
        }
        else if (paymentLinks.type().equalsIgnoreCase("CARD")) {
            PaymentStatusCardType cardType = paymentLinks.cardType();
            orderHistory.setCardBrand(cardType.issuer());
            orderHistory.setCardHolderName(cardType.cardHolderName());
            orderHistory.setCardType(cardType.cardType());
            orderHistory.setIssuer(cardType.issuer());
            orderHistory.setChannel("Card");
            orderHistory.setCardNo(cardType.lastFourDigits());

        }


        orderHistory.setOrderStatus(OrderStatus.PAID.name());
        orderHistoryRepository.save(orderHistory);

        eventPublisher.publishEvent(new SubscriptionEvents(this, orderHistory));
    }

    public void successfullMobilePayment(Object payload) {
        ZohoPaymentResponse paymentLinks = (ZohoPaymentResponse) payload;
        if (paymentLinks != null) {
            String[] hostelIdSessionId = paymentLinks.paymentSessionId().split("-");
            String sessionId = hostelIdSessionId[hostelIdSessionId.length - 1];

            PaymentSessions paymentSessions = paymentSessionService.getPaymentSessionBySessionId(sessionId);

            if (paymentSessions != null) {
                OrderHistory orderHistory = new OrderHistory();
                orderHistory.setHostelId(paymentSessions.getHostelId());
                orderHistory.setPaymentSessionId(paymentSessions.getPaymentSessionId());
                orderHistory.setDiscountAmount(paymentSessions.getDiscountAmount());
                orderHistory.setPlanAmount(paymentSessions.getPlanAmount());
                orderHistory.setPlanCode(paymentSessions.getPlanCode());
                orderHistory.setTotalAmount(paymentSessions.getPaymentAmount());
                orderHistory.setUserType(UserType.OWNER.name());
                orderHistory.setPaidBy(paymentSessions.getCreatedBy());
                orderHistory.setCreatedAt(new Date());
                orderHistory.setCreatedBy(paymentSessions.getCreatedBy());
                orderHistory.setActive(true);

                orderHistory.setPaymentType(paymentLinks.type());
                if (paymentLinks.type().equalsIgnoreCase("UPI")) {
                    PaymentStatus status = paymentLinks.upiStatus();
                    orderHistory.setChannel(status.channel());
                    orderHistory.setUpiId(status.id());
                }
                else if (paymentLinks.type().equalsIgnoreCase("CARD")) {
                    PaymentStatusCardType cardType = paymentLinks.cardType();
                    orderHistory.setCardBrand(cardType.issuer());
                    orderHistory.setCardHolderName(cardType.cardHolderName());
                    orderHistory.setCardType(cardType.cardType());
                    orderHistory.setIssuer(cardType.issuer());
                    orderHistory.setChannel("Card");
                    orderHistory.setCardNo(cardType.lastFourDigits());

                }

                orderHistory.setOrderStatus(OrderStatus.PAID.name());
                OrderHistory oh = orderHistoryRepository.save(orderHistory);

                eventPublisher.publishEvent(new SubscriptionEvents(this, oh));
            }

        }
    }

    public List<OrderHistory> findByHostelIdOrderByCreatedAtDesc(String hostelId) {
        List<OrderHistory> listOrderHistory = orderHistoryRepository.findByHostelIdOrderByCreatedAtDesc(hostelId);
        if (listOrderHistory == null) {
            listOrderHistory = new ArrayList<>();
        }
        return listOrderHistory;
    }

    public List<OrderHistory> findOrderHistoryByOrderHistoryId(List<Long> listOrderIds) {
        List<OrderHistory> listOrderHistories = orderHistoryRepository.findAllById(listOrderIds);
        if (listOrderHistories == null) {
            listOrderHistories = new ArrayList<>();
        }
        return listOrderHistories;
    }
}
