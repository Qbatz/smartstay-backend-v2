package com.smartstay.smartstay.util;

import com.smartstay.smartstay.ennum.ActivitySource;
import com.smartstay.smartstay.ennum.ActivitySourceType;

public class ActivityLogUtils {
    public static String getActivityDescription(String activitySource, String operationType) {
        if (activitySource.equalsIgnoreCase(ActivitySource.PROFILE.name())) {
            if (operationType.equalsIgnoreCase(ActivitySourceType.LOGGED_IN.name())) {
                return "Logged in to smartstay";
            }
            else if (operationType.equalsIgnoreCase(ActivitySourceType.CREATE.name())) {
                return "Created Account in smartstay";
            }
        }
        if (activitySource.equalsIgnoreCase(ActivitySource.AMENITY.name())) {
            if (operationType.equalsIgnoreCase(ActivitySourceType.CREATE.name())) {
                return "Created amenity";
            }
            else if (operationType.equalsIgnoreCase(ActivitySourceType.ASSIGN.name())) {
                return "Amenity has been assigned";
            }
            else if (operationType.equalsIgnoreCase(ActivitySourceType.UPDATE.name())) {
                return "Amenity has been updated";
            }
            else if (operationType.equalsIgnoreCase(ActivitySourceType.UNASSIGN.name())) {
                return "Amenity has been un-assigned";
            }
            else if (operationType.equalsIgnoreCase(ActivitySourceType.DELETE.name())) {
                return "Amenity has been deleted";
            }
        }
        if (activitySource.equalsIgnoreCase(ActivitySource.SETTLEMENT.name())) {
            if (operationType.equalsIgnoreCase(ActivitySourceType.CREATE.name())) {
                return "Generated final settlement";
            }
            else if (operationType.equalsIgnoreCase(ActivitySourceType.UPDATE.name())) {
                return "Updated final settlement";
            }
        }
        if (activitySource.equalsIgnoreCase(ActivitySource.ASSETS.name())) {
            if (operationType.equalsIgnoreCase(ActivitySourceType.CREATE.name())) {
                return "Added new assets";
            }
            else if (operationType.equalsIgnoreCase(ActivitySourceType.UPDATE.name())) {
                return "Assets has been updated";
            }
        }

        if (activitySource.equalsIgnoreCase(ActivitySource.EXPENSE_CATEGORY.name())) {
            return getExpenseCategoryOperations(operationType);
        }
        if (activitySource.equalsIgnoreCase(ActivitySource.EXPENSE_SUB_CATEGORY.name())) {
            return getExpenseSubCategoryOperations(operationType);
        }

        return null;
    }

    private static String getExpenseSubCategoryOperations(String operationType) {
        if (operationType.equalsIgnoreCase(ActivitySourceType.CREATE.name())) {
            return "Added expense subcategories";
        }
        else if (operationType.equalsIgnoreCase(ActivitySourceType.UPDATE.name())) {
            return "Updated expense subcategory";
        }
        return null;
    }


    private static String getExpenseCategoryOperations(String operationName) {
        if (operationName.equalsIgnoreCase(ActivitySourceType.CREATE.name())) {
            return "Created expense category";
        }
        else if (operationName.equalsIgnoreCase(ActivitySourceType.UPDATE.name())) {
            return "Updated expense category";
        }
        return null;
    }



}
