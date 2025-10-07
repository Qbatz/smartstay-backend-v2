package com.smartstay.smartstay.events;

import org.springframework.context.ApplicationEvent;

public class HostelEvents extends ApplicationEvent {

    private final String hostelId;
    private final String userId;
    private final String parentId;

    public HostelEvents(Object source, String hostelId, String userId, String parentId) {
        super(source);
        this.hostelId = hostelId;
        this.userId = userId;
        this.parentId = parentId;
    }

    public String getHostelId() {
        return hostelId;
    }

    public String getUserId() {
        return userId;
    }

    public String getParentId() {
        return parentId;
    }
}
