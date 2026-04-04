package com.smartstay.smartstay.events;

import com.smartstay.smartstay.dao.OrderHistory;
import org.springframework.context.ApplicationEvent;

public class SubscriptionEvents extends ApplicationEvent {
    private OrderHistory orderHistory = null;
    private String createdBy = null;
    public SubscriptionEvents(Object source, OrderHistory orderHistory, String createdBy) {
        super(source);
        this.orderHistory = orderHistory;
        this.createdBy = createdBy;
    }

    public OrderHistory getOrderHistory() {
        return orderHistory;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}
