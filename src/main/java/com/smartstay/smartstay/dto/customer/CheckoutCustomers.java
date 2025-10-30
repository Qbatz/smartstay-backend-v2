package com.smartstay.smartstay.dto.customer;

import java.util.Date;

public interface CheckoutCustomers {
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
    String getFloorName();
    String getRoomName();
    String getBedName();
    String getCustomerId();
    String getCountryCode();
    Date getActualJoiningDate();
    Date getJoiningDate();
    Date getCreatedAt();
    Date getExpectedJoiningDate();
    Date getCheckoutDate();
}
