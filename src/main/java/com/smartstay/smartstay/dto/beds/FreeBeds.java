package com.smartstay.smartstay.dto.beds;

import java.util.Date;

public interface FreeBeds {
    Integer getBedId();
    String getBedName();
    Integer getFloorId();
    Integer getRoomId();
    String getRoomName();
    String getFloorName();
    String getBedStatus();
    Date getExpectedJoiningDate();
    Date getLeavingDate();
}
