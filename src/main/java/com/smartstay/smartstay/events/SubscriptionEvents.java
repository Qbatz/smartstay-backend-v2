package com.smartstay.smartstay.events;

import com.smartstay.smartstay.dao.OrderHistory;
import org.springframework.context.ApplicationEvent;

public class SubscriptionEvents extends ApplicationEvent {
    private OrderHistory orderHistory = null;
    public SubscriptionEvents(Object source, OrderHistory orderHistory) {
        super(source);
        this.orderHistory = orderHistory;
    }

    public OrderHistory getOrderHistory() {
        return orderHistory;
    }

}
