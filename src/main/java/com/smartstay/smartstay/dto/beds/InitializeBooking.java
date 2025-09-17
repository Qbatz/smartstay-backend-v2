package com.smartstay.smartstay.dto.beds;

public interface InitializeBooking {
    Integer getBedId();
    Integer getRoomId();
    Integer getFloorId();
    String getBedName();
    String getFloorName();
    String getRoomName();
    Double getRentAmount();
    String getCurrentStatus();
}
