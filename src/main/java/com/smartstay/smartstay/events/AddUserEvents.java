package com.smartstay.smartstay.events;

import org.springframework.context.ApplicationEvent;

public class AddUserEvents extends ApplicationEvent {

    private final String userId;
    private final String hostelId;
    private final String fullName;
    private final String parentId;
    public AddUserEvents(Object source, String userId, String hostelId, String fullName, String parentId) {
        super(source);
        this.fullName = fullName;
        this.userId = userId;
        this.hostelId = hostelId;
        this.parentId = parentId;
    }

    public String getUserId() {
        return userId;
    }

    public String getHostelId() {
        return hostelId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getParentId() {
        return parentId;
    }
}
