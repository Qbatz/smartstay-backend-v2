package com.smartstay.smartstay.events;

import org.springframework.context.ApplicationEvent;

public class AddAdminEvents extends ApplicationEvent {

    private final String parentId;
    private final String adminId;
    private final String adminName;

    public AddAdminEvents(Object source,  String parentId, String adminUserId, String adminFullName) {
        super(source);
        this.parentId = parentId;
        this.adminId = adminUserId;
        this.adminName = adminFullName;
    }

    public String getParentId() {
        return parentId;
    }

    public String getAdminId() {
        return adminId;
    }

    public String getAdminName() {
        return adminName;
    }
}
