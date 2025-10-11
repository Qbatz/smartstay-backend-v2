package com.smartstay.smartstay.dto.booking;

import java.util.Date;

public interface BookedCustomer {
    String getFirstName();
    String getLastName();
    String getCustomerId();
    Integer getBedId();
    Integer getRoomId();
    Integer getFloorId();
    String getRoomName();
    String getBedName();
    String getFloorName();
    Date getJoiningDate();
    Date getLeavingDate();
}
