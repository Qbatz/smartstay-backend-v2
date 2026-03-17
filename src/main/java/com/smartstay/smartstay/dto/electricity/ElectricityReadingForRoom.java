package com.smartstay.smartstay.dto.electricity;

import java.util.Date;

public interface ElectricityReadingForRoom {
    Integer getId();
    Double getConsumption();
    Double getCurrentReading();
    Double getUnitPrice();
    Date getEntryDate();
    String getHostelId();
    Integer getRoomId();
    Date getStartDate();
}
