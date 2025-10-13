package com.smartstay.smartstay.dto.electricity;

import java.util.Date;

public interface ElectricityCustomersList {
    String getCustomerId();
    String getFloorName();
    String getRoomName();
    String getBedName();
    String getFirstName();
    String getLastName();
    String getProfilePic();
    Double getAmount();
    Integer getBedId();
    Integer getFloorId();
    Integer getRoomId();
    Date getStartDate();
    Date getEndDate();
    Long getId();
    Double getConsumption();
}
