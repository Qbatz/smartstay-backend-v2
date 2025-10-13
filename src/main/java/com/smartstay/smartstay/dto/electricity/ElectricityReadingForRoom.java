package com.smartstay.smartstay.dto.electricity;

import java.util.Date;

public interface ElectricityReadingForRoom {
    Integer getId();
    Integer getConsumption();
    Integer getCurrentReading();
    Double getUnitPrice();
    Date getEntryDate();
    String getHostelId();
    Integer getRoomId();
    Date getStartDate();
}
