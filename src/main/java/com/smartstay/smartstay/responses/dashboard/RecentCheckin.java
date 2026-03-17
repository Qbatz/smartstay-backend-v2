package com.smartstay.smartstay.responses.dashboard;

import java.util.Date;

public record RecentCheckin(String tenantId,String initials,String customerName, String profilePic, String roomName, String sharingType, String bedName, String joiningDate,
        String status) {
}
