package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.OrderHistory;
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

import java.util.Date;

@Service
public class OrderHistoryService {
    @Autowired
    private OrderHistoryRepository orderHistoryRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

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
}
