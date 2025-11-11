package com.smartstay.smartstay.dto.customer;

import java.util.Date;

public interface CustomersBookingDetails {
    Integer getBedId();
    Integer getRoomId();
    Integer getFloorId();
    Double getRentAmount();
    Double getBookingAmount();
    Date getCheckoutDate();
    Date getRequestedCheckoutDate();
    Date getLeavingDate();
    Date getNoticeDate();
    String getBookingId();
    String getCurrentStatus();
    String getReasonForLeaving();
    Date getExpecteJoiningDate();
    Date getJoiningDate();
    String getFirstName();
    String getLastName();
    String getRoomName();
    String getFloorName();
    String getBedName();
}
