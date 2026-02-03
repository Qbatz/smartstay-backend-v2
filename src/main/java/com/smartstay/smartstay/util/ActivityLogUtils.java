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
        if (activitySource.equalsIgnoreCase(ActivitySource.BANKING.name())) {
            return getBankingOperations(operationType);
        }
        if (activitySource.equalsIgnoreCase(ActivitySource.BEDS.name())) {
            return getBedsOperations(operationType);
        }

        if (activitySource.equalsIgnoreCase(ActivitySource.BOOKING.name())) {
            return getBedsOperations(operationType);
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

    private static String getBankingOperations(String operationName) {
        if (operationName.equalsIgnoreCase(ActivitySourceType.CREATE.name())) {
            return "Added a new bank account";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.UPDATE.name())) {
            return "Updated bank account";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.ADD_MONEY.name())) {
            return "Added money to the account";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.TRANSFER.name())) {
            return "Transferred money from ";
        }

        return null;
    }

    private static String getBedsOperations(String operationName) {
        if (operationName.equalsIgnoreCase(ActivitySourceType.CREATE.name())) {
            return "Created a new bed";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.UPDATE.name())) {
            return "Updated a bed";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.UPDATE_AMOUNT.name())) {
            return "Updated amount for bed";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.DELETE.name())) {
            return "Deleted a bed";
        }
        return null;
    }

    private static String getBookingsOperations(String operationName) {
        if (operationName.equalsIgnoreCase(ActivitySourceType.CANCEL.name())) {
            return "Cancelled a booking";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.CHECKOUT.name())) {
            return "Checkout a customer ";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.UPDATE_AMOUNT.name())) {
            return "Updated rent amount for";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.JOINING_DATE.name())) {
            return "Updated joining date for";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.ADVANCE_AMOUNT.name())) {
            return "Updated advance amount for";
        }
        return null;
    }

}
