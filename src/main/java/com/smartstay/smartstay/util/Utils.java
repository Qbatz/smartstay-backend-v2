package com.smartstay.smartstay.util;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class Utils {

    public static final String ENVIRONMENT_LOCAL = "LOCAL";
    public static final String ENVIRONMENT_DEV = "DEV";
    public static final String ENVIRONMENT_QA = "QA";
    public static final String ENVIRONMENT_PROD = "PROD";

    private static final String DATE_FORMAT = "dd/MM/yyyy";

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
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

    public static String dateToString(Date date) {
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
}
