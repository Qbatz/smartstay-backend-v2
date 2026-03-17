package com.smartstay.smartstay.responses.dashboard;

import java.util.Date;

public record RecentRequest(Long requestId, String customerName, String profilePic, String type, String status,
        String date) {
}
