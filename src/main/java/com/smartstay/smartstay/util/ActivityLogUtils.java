package com.smartstay.smartstay.util;

import com.smartstay.smartstay.ennum.ActivitySource;
import com.smartstay.smartstay.ennum.ActivitySourceType;

public class ActivityLogUtils {
    public static String getActivityDescription(String activitySource, String operationType) {
        if (activitySource.equalsIgnoreCase(ActivitySource.PROFILE.name())) {
            if (operationType.equalsIgnoreCase(ActivitySourceType.LOGGED_IN.name())) {
                return "Logged in to smartstay";
            }
            if (operationType.equalsIgnoreCase(ActivitySourceType.CREATE.name())) {
                return "Created Account in smartstay";
            }
        }
        return null;
    }
}
