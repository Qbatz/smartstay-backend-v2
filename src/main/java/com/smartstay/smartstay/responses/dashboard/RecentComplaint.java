package com.smartstay.smartstay.responses.dashboard;

import java.util.Date;

public record RecentComplaint(Integer complaintId, String customerName, String profilePic, String type, String status,
        Date date) {
}
