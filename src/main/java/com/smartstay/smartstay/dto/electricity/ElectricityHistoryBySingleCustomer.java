package com.smartstay.smartstay.dto.electricity;

import java.util.Date;

public interface ElectricityHistoryBySingleCustomer {
    Long getId();
    Date getStartDate();
    Date getEndDate();
    Double getConsumption();
    Double getAmount();
    String getBedName();
    String getFloorName();
    String getRoomName();

}
