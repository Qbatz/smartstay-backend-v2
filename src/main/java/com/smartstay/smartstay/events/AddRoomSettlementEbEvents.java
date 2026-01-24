package com.smartstay.smartstay.events;

import org.springframework.context.ApplicationEvent;

import java.util.Date;

public class AddRoomSettlementEbEvents extends ApplicationEvent {
    private String hostelId;
    private String customerId;
    private Date endDate = null;
    private String createdBy = null;

    public AddRoomSettlementEbEvents(Object source, String hostelId, String customerId, Date endDate, String createdBy) {
        super(source);
        this.hostelId = hostelId;
        this.customerId = customerId;
        this.endDate = endDate;
        this.createdBy = createdBy;
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

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
