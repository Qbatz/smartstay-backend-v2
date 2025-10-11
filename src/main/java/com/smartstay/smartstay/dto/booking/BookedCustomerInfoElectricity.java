package com.smartstay.smartstay.dto.booking;

import java.util.Date;

public interface BookedCustomerInfoElectricity {
    String getCustomerId();
    String getHostelId();
    Date getJoiningDate();
    Date getLeavingDate();
    Integer getRoomId();
    Integer getBedId();
    Integer getFloorId();
    String getFloorName();
    String getBedName();
    String getRoomName();
}
