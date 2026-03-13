package com.smartstay.smartstay.responses.dashboard;

import java.util.Date;

public record RecentCheckin(String customerName, String profilePic, String roomName, String bedName, Date joiningDate,
        String status) {
}
