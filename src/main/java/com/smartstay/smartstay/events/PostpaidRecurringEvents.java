package com.smartstay.smartstay.events;

import org.springframework.context.ApplicationEvent;

public class PostpaidRecurringEvents extends ApplicationEvent {
    private String hostelId;
    public PostpaidRecurringEvents(Object source, String hostelId) {
        super(source);
        this.hostelId = hostelId;
    }

    public String getHostelId() {
        return hostelId;
    }

    public void setHostelId(String hostelId) {
        this.hostelId = hostelId;
    }
}
