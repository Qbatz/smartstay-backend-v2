package com.smartstay.smartstay.dto.electricity;

import java.util.Date;

public interface ElectricityReaddings {
    Integer getId();
    Double getConsumption();
    Double getCurrentReading();
    Double getUnitPrice();
    Date getEntryDate();
    String getHostelId();
    Integer getRoomId();
    Integer getFloorId();
    String getRoomName();
    String getFloorName();
    Integer getNoOfTenants();
}
