package com.smartstay.smartstay.dto.customer;

import java.util.Date;

public interface CustomerData {
    String getFirstName();
    String getCity();
    String getState();
    String getCountry();
    String getMobile();
    String getCurrentStatus();
    String getEmailId();
    String getProfilePic();
    String getBedId();
    String getFloorId();
    String getRoomId();
    String getCustomerId();
    Date getActualJoiningDate();
    Date getJoiningDate();
    Date getCreatedAt();
}