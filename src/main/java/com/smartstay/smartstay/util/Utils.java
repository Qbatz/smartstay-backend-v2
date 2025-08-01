package com.smartstay.smartstay.util;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

public class Utils {

    public static final String ENVIRONMENT_LOCAL = "LOCAL";
    public static final String ENVIRONMENT_DEV = "DEV";
    public static final String ENVIRONMENT_QA = "QA";
    public static final String ENVIRONMENT_PROD = "PROD";

    private static final String DATE_FORMAT = "dd/MM/yyyy";

    public static final String ACCESS_RESTRICTED = "Access Restricted";
    public static final String UN_AUTHORIZED = "Unauthorized";
    public static final String INVALID = "Invalid";
    public static final String UPDATED = "Updated Successfully";
    public static final String CREATED = "Created Successfully";
    public static final String EMAIL_ID_EXISTS = "Email Id already registered";
    public static final String MOBILE_NO_EXISTS = "Mobile number already registered";
    public static final String INVALID_ROLE = "Invalid role";
    public static final String INVALID_CUSTOMER_ID = "Invalid customer id";
    public static final String RESTRICTED_HOSTEL_ACCESS = "Do not have the access to access this hostel";
    public static final String N0_FLOOR_FOUND_HOSTEL = "No floor found for the specified hostel.";
    public static final String N0_ROOM_FOUND_FLOOR = "No room found for the specified floor.";
    public static final String N0_BED_FOUND_ROOM = "No bed found for the specified room.";

    public static final String PERMISSION_READ = "READ";
    public static final String PERMISSION_WRITE = "WRITE";
    public static final String PERMISSION_UPDATE = "UPDATE";
    public static final String PERMISSION_DELETE = "DELETE";


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
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    public static int generateOtp() {
        return (int)(Math.random() * 900000) + 100000;
    }
    public static boolean verifyEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

//    this accepts only yyyy-MM-dd format only
    public static Date stringToDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format");
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
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }


    public static boolean compareWithTodayDate(Date date2) {
        String dateString = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        Date today;
        try {
            today = new SimpleDateFormat(DATE_FORMAT).parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date");
        }

        return today.before(date2) || today.compareTo(date2) == 0;
    }

    public static String generateRandomUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static int compareWithTwoDates(Date date1, Date date2) {
        return date1.compareTo(date2);
    }
}
