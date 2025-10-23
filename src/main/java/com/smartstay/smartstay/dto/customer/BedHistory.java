package com.smartstay.smartstay.dto.customer;

import java.util.Date;

public interface BedHistory {
    public Long getHistoryId();
    Integer getBedId();
    String getBedName();
    String getRoomName();
    Integer getRoomId();
    Date getStartDate();
    Date getEndDate();
    String getReason();
    Double getRent();
    String getType();

}
