package com.smartstay.smartstay.responses.amenitity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponse {
    private String customerId;
    private String customerName;
    private String initials;
    private String profilePic;
    private String mobile;
    private String countryCode;
    private String bedName;
    private String floorName;
    private String roomName;
    private boolean canAssign;
    private boolean isEnding;
    private String endDate;
}

