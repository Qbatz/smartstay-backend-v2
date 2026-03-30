package com.smartstay.smartstay.events;

import org.springframework.context.ApplicationEvent;

public class JoiningBasedPrepaidEvents extends ApplicationEvent {
    private String hostelId = null;
    private String customerId = null;
    public JoiningBasedPrepaidEvents(Object source, String hostelId, String customerId) {
        super(source);
        this.hostelId = hostelId;
        this.customerId = customerId;
    }

    public String getHostelId() {
        return hostelId;
    }

    public void setHostelId(String hostelId) {
        this.hostelId = hostelId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
