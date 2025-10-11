package com.smartstay.smartstay.dto.electricity;

import java.util.Date;

public interface RoomElectricityCustomersList {
    String getCustomerId();
    Long getId();
    Double getAmount();
    Long getBedId();
    Double getConsumption();
    Date getStartDate();
    Date getEndDate();
    String getFirstName();
    String getLastName();
    String getBedName();
    String getProfilePic();
}
