package com.smartstay.smartstay.events;

import org.springframework.context.ApplicationEvent;

public class RecurringEvents extends ApplicationEvent {

    private String hostelId;
    public RecurringEvents(Object source, String hostelId) {
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
