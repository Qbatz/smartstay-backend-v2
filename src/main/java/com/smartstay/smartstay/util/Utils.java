package com.smartstay.smartstay.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

public class Utils {

    public static final String ENVIRONMENT_LOCAL = "LOCAL";
    public static final String ENVIRONMENT_DEV = "DEV";
    public static final String ENVIRONMENT_QA = "QA";
    public static final String ENVIRONMENT_PROD = "PROD";

    public static final String USER_INPUT_DATE_FORMAT = "dd-MM-yyyy";

    public static final String OUTPUT_DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATE_FORMAT_YY = "yyyy/MM/dd";
    public static final String DATE_FORMAT_ZOHO = "yyyy-MM-dd";

    public static final String ACCESS_RESTRICTED = "Access Restricted";
    public static final String UN_AUTHORIZED = "Unauthorized";
    public static final String INVALID = "Invalid";
    public static final String INVALID_EMAIL = "Invalid Email";
    public static final String INVALID_VENDOR = "Invalid Vendor";
    public static final String INVALID_BANKING = "Invalid Banking";
    public static final String INVALID_ASSET = "Invalid Asset";
    public static final String INVALID_FLOOR = "Invalid Floor";
    public static final String INVALID_USER = "Invalid User";
    public static final String INVALID_OTP = "Invalid Otp";
    public static final String OTP_EXPIRED = "Otp Expired";
    public static final String ASSET_NAME_ALREADY_EXISTS = "Asset name already exists";
    public static final String SERIAL_NUMBER_ALREADY_EXISTS = "Serial number already exists";
    public static final String PASSWORD_RESET_SUCCESS = "Password reset successfully.";
    public static final String PASSWORD_CHANGED_SUCCESS = "Password changed successfully";
    public static final String USER_NOT_FOUND = "User not found.";
    public static final String UPDATED = "Updated Successfully";
    public static final String USER_ASSIGNED = "User Assigned Successfully";
    public static final String ASSIGNED = "Asset Assigned Successfully";
    public static final String STATUS_UPDATED = "Status Updated Successfully";
    public static final String CREATED = "Created Successfully";
    public static final String DELETED = "Deleted Successfully";
    public static final String EMAIL_ID_EXISTS = "Email Id already registered";
    public static final String MOBILE_NO_EXISTS = "Mobile number already registered";
    public static final String INVALID_ROLE = "Invalid role";
    public static final String ROLE_NAME_EXISTS = "Role name already exists";
    public static final String ACTIVE_USERS_FOUND = "Active users found";
    public static final String INVALID_CUSTOMER_ID = "Invalid customer id";
    public static final String CUSTOMER_ON_NOTICE = "Customer is already on notice";
    public static final String RESTRICTED_HOSTEL_ACCESS = "Do not have the access to access this hostel";
    public static final String N0_FLOOR_FOUND_HOSTEL = "No floor found for the specified hostel.";
    public static final String N0_ROOM_FOUND_FLOOR = "No room found for the specified floor.";
    public static final String N0_BED_FOUND_ROOM = "No bed found for the specified room.";
    public static final String BED_CURRENTLY_UNAVAILABLE = "Bed is unavailable";
    public static final String BED_UNAVAILABLE_DATE = "Bed is unavailable for selected date";
    public static final String CHECK_IN_FUTURE_DATE_ERROR = "Check in cannot be accept for future dates";
    public static final String OTP_SENT_SUCCESSFULLY = "OTP has been sent successfully.";
    public static final String PAYLOADS_REQUIRED = "Payloads required";
    public static final String INVALID_BED_ID = "Invalid bed id passed";
    public static final String CANNOT_DELETE_DEFAULT_ROLES = "Cannot delete default roles";
    public static final String CANNOT_EDIT_DEFAULT_ROLES = "Cannot edit default roles";
    public static final String ACCOUNT_NO_ALREAY_EXISTS = "Account number already exists";

    public static final String NO_ACCOUNT_NO_FOUND = "No account number found";
    public static final String CUSTOMER_ALREADY_CHECKED_IN = "Customer is already checked in";
    public static final String CUSTOMER_ALREADY_BOOKED = "Customer is already Booked";
    public static final String CUSTOMER_CHECKED_IN_INACTIVE_STATUS = "Cannot changed status to inactive for checked in customers";

    public static final String PERMISSION_READ = "READ";
    public static final String PERMISSION_WRITE = "WRITE";
    public static final String PERMISSION_UPDATE = "UPDATE";
    public static final String PERMISSION_DELETE = "DELETE";
    public static final String SMART_STAY_SUPER_ADMIN = "SmartStay - Super Admin";
    public static final String SMART_STAY_ADMIN = "SmartStay - Admin";
    public static final String READ_ONLY = "readOnly";
    public static final String WRITE_ONLY = "writeOnly";
    public static final String IN_PROGRESS = "INPROGRESS";
    public static final String PENDING = "PENDING";
    public static final String RESOLVED = "RESOLVED";


    /**
     *  Defining module Id's here
     *
     *  while doing so correct on smartstayApplication.java file aswell.
     *
     *   This always linked to the db
     */

    public static int MODULE_ID_DASHBOARD = 1;
    public static int MODULE_ID_ANNOUNCEMENT = 2;
    public static int MODULE_ID_UPDATES = 3;
    public static int MODULE_ID_PAYING_GUEST = 4;
    public static int MODULE_ID_CUSTOMERS = 5;
    public static int MODULE_ID_BOOKING = 6;
    public static int MODULE_ID_CHECKOUT = 7;
    public static int MODULE_ID_WALK_IN = 8;
    public static int MODULE_ID_ASSETS = 9;
    public static int MODULE_ID_VENDOR = 10;
    public static int MODULE_ID_BILLS = 11;
    public static int MODULE_ID_RECURRING_BILLS = 12;
    public static int MODULE_ID_COMPLAINTS = 13;
    public static int MODULE_ID_ELECTRIC_CITY = 14;
    public static int MODULE_ID_EXPENSE = 15;
    public static int MODULE_ID_REPORTS = 16;
    public static int MODULE_ID_BANKING = 17;
    public static int MODULE_ID_PROFILE = 18;
    public static int MODULE_ID_AMENITIES = 19;

    public static int MODULE_ID_RECEIPT = 20;
    public static int MODULE_ID_INVOICE = 21;
    public static int MODULE_ID_USER = 22;
    public static int MODULE_ID_ROLES = 23;
    public static int MODULE_ID_AGREEMENT = 24;
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    public static int generateOtp() {
        return (int)(Math.random() * 900000) + 100000;
    }
    public static boolean verifyEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static Date stringToDate(String date, String inputFormat) {
        try {
            return new SimpleDateFormat(inputFormat).parse(date);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format");
        }
    }


    //this accepts only dd-MM-yyyy format
    public static String stringToDateFormat(String inputDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = inputFormat.parse(inputDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format. Expected format is dd-MM-yyyy.");
        }
    }


    public static Date convertStringToDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String dateToString(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(OUTPUT_DATE_FORMAT).format(date);
    }


    public static boolean compareWithTodayDate(Date date2) {
        String dateString = new SimpleDateFormat(OUTPUT_DATE_FORMAT).format(new Date());
        Date today;
        try {
            today = new SimpleDateFormat(OUTPUT_DATE_FORMAT).parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date");
        }

        return today.before(date2) || today.compareTo(date2) == 0;
    }

    public static String generateRandomUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }


    public static int calculateRemainingDays(Date nextBillingAt) {
        if (nextBillingAt == null) return 0;

        LocalDate billingDate = nextBillingAt.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        long remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), billingDate);
        return (int) Math.max(0, remainingDays);

    }


    public static int compareWithTwoDates(Date date1, Date date2) {
        return date1.compareTo(date2);
    }

    public static boolean checkNullOrEmpty(Object data) {
        if (data == null) {
            return false;
        }
        if (data instanceof String) {
            if (((String)data).equalsIgnoreCase("")) {
                return false;
            }
        }
        if (data instanceof Integer) {
            if ((Integer) data == 0) {
                return false;
            }
        }


        return true;
    }

    public static long findNumberOfDays(Date date1, Date date2) {
        LocalDate start = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end   = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return ChronoUnit.DAYS.between(start, end);
    }
}
