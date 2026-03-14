package com.smartstay.smartstay.responses.dashboard;

import java.util.Date;

public record RecentComplaint(Integer complaintId, String customerName, String profilePic, String roomName, String type, String status,
        String date, String description) {
}
