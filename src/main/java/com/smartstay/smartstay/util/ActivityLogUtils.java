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
            else if (operationType.equalsIgnoreCase(ActivitySourceType.SETUP.name())) {
                return "Setup the login pin";
            }
            else if (operationType.equalsIgnoreCase(ActivitySourceType.ADD_ADMIN.name())) {
                return "Added admin user";
            }
            else if (operationType.equalsIgnoreCase(ActivitySourceType.ADD_USER.name())) {
                return "Added admin user";
            }
            else if (operationType.equalsIgnoreCase(ActivitySourceType.UPDATE.name())) {
                return "Updated Profile information";
            }
            else if (operationType.equalsIgnoreCase(ActivitySourceType.CHANGE_SELF_PASSWORD.name())) {
                return "Changes the password";
            }
            else if (operationType.equalsIgnoreCase(ActivitySourceType.CHANGE_ADMIN_PASSWORD.name())) {
                return "Changes the admin password";
            }
            else if (operationType.equalsIgnoreCase(ActivitySourceType.DELETE_ADMIN_USER.name())) {
                return "Deleted the admin user";
            }
            else if (operationType.equalsIgnoreCase(ActivitySourceType.LOGOUT.name())) {
                return "Logged out from the application";
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
        if (activitySource.equalsIgnoreCase(ActivitySource.EXPENSE.name())) {
            return getExpenseOperations(operationType);
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
            return getBookingsOperations(operationType);
        }

        if (activitySource.equalsIgnoreCase(ActivitySource.COMPLAINTS.name())) {
            return getComplaintsOperations(operationType);
        }
        if (activitySource.equalsIgnoreCase(ActivitySource.COMMENTS.name())) {
            return getCommentOperations(operationType);
        }
        if (activitySource.equalsIgnoreCase(ActivitySource.COMPLAINT_TYPE.name())) {
            return getComplaintTypeDescription(operationType);
        }
        if (activitySource.equalsIgnoreCase(ActivitySource.CUSTOMERS.name())) {
            return getCustomerDescription(operationType);
        }
        if (activitySource.equalsIgnoreCase(ActivitySource.ELECTRICITY.name())) {
            return getElectricityOperation(operationType);
        }
        if (activitySource.equalsIgnoreCase(ActivitySource.FLOORS.name())) {
            return getFloorsOperations(operationType);
        }
        if (activitySource.equalsIgnoreCase(ActivitySource.HOSTEL.name())) {
            return getHostelsOperations(operationType);
        }
        if (activitySource.equalsIgnoreCase(ActivitySource.INVOICE.name())) {
            return getInvoiceOperations(operationType);
        }
        if (activitySource.equalsIgnoreCase(ActivitySource.ROLE.name())) {
            return getRolesOperation(operationType);
        }
        if (activitySource.equalsIgnoreCase(ActivitySource.ROOMS.name())) {
            return getRoomsOperation(operationType);
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

    private static String getComplaintsOperations(String opeationName) {
        if (opeationName.equalsIgnoreCase(ActivitySourceType.CREATE.name())) {
            return "Complain has been raised behalf of";
        }
        return null;
    }

    private static String getCommentOperations(String operationName) {
        if (operationName.equalsIgnoreCase(ActivitySourceType.CREATE.name())) {
            return "Added comments for ";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.UPDATE.name())) {
            return "Complaint has been updated";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.ASSIGN.name())) {
            return "Complaint has been assigned to";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.DELETE.name())) {
            return "Complaint has been deleted by";
        }

        return null;
    }

    private static String getComplaintTypeDescription(String operationName) {
        if (operationName.equalsIgnoreCase(ActivitySourceType.CREATE.name())) {
            return "Created complaint type";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.UPDATE.name())) {
            return "Updated the complaint type";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.DELETE.name())) {
            return "Deleted the complaint type";
        }
        return null;
    }

    private static String getCustomerDescription(String operationName) {
        if (operationName.equalsIgnoreCase(ActivitySourceType.CHECKIN.name())) {
            return "Checked in a customer";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.CREATE.name())) {
            return "Added a walk in customer";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.UPDATE.name())) {
            return "Updated customer information's";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.ASSIGN.name())) {
            return "Assigned a bed for";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.BOOKING.name())) {
            return "Created a booking for";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.NOTICE.name())) {
            return "Moved a customer to notice";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.SETTLEMENT.name())) {
            return "Generated settlement for";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.CHANGED_BED.name())) {
            return "Changed the bed for";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.CANCEL.name())) {
            return "Cancelled the checkout";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.DELETE.name())) {
            return "Deleted user.";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.FILES_UPLOAD.name())) {
            return "Files uploaded";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.FILE_DELETE.name())) {
            return "Deleted a file";
        }
        return null;
    }

    private static String getElectricityOperation(String operationName) {
        if (operationName.equalsIgnoreCase(ActivitySourceType.CREATE.name())) {
            return "Electricity reading added";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.UPDATE.name())) {
            return "Updated electricity date";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.UPDATE_READING.name())) {
            return "Updated electricity";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.DELETE.name())) {
            return "Deleted electricity entry";
        }
        return null;
    }

    private static String getExpenseOperations(String operationName) {
        if (operationName.equalsIgnoreCase(ActivitySourceType.CREATE.name())) {
            return "New expense has been added";
        }
        return null;
    }

    private static String getFloorsOperations(String operationName) {
        if (operationName.equalsIgnoreCase(ActivitySourceType.CREATE.name())) {
            return "New Floor has been added";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.UPDATE.name())) {
            return "Floor has updated by";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.DELETE.name())) {
            return "Floor has been deleted";
        }
        return null;
    }

    public static String getHostelsOperations(String operationName) {
        if (operationName.equalsIgnoreCase(ActivitySourceType.CREATE.name())) {
            return "Created a hostel";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.REMOVE_USER.name())) {
            return "Removed user ";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.DELETE.name())) {
            return "Deleted hostel";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.UPDATE_EB_AMOUNT.name())) {
            return "Updated EB amount";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.UPDATE_EB_CONFIG.name())) {
            return "Updated EB configuration";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.UPDATE_BILLING_CONFIG.name())) {
            return "Updated billing rules";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.UPDATE.name())) {
            return "Updated hostel information";
        }

        return null;
    }

    private static String getInvoiceOperations(String operation) {
        if (operation.equalsIgnoreCase(ActivitySourceType.MANUAL_BILL.name())) {
            return "Created a manual invoice";
        }
        if (operation.equalsIgnoreCase(ActivitySourceType.UPDATE.name())) {
            return "Updated invoice";
        }
        return null;
    }

    private static String getRolesOperation(String operationName) {
        if (operationName.equalsIgnoreCase(ActivitySourceType.CREATE.name())) {
            return "Created a new Role";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.UPDATE.name())) {
            return "Updated a role";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.DELETE.name())) {
            return "Deleted a role";
        }
        return null;
    }
    private static String getRoomsOperation(String operationName) {
        if (operationName.equalsIgnoreCase(ActivitySourceType.CREATE.name())) {
            return "Created a new Room";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.UPDATE.name())) {
            return "Updated a room";
        }
        if (operationName.equalsIgnoreCase(ActivitySourceType.DELETE.name())) {
            return "Deleted a room";
        }
        return null;
    }
}
