package com.smartstay.smartstay.dto.electricity;

import java.util.Date;

public interface ElectricityReaddings {
    Integer getId();
    Double getConsumption();
    Date getCreatedAt();
    Double getCurrentReading();
    Double getUnitPrice();
    Date getEntryDate();
    String getHostelId();
    Double getPreviousReadings();
    Integer getRoomId();
    Integer getFloorId();
    String getRoomName();
    String getFloorName();
    String getFirstName();
    String getLastName();
}
