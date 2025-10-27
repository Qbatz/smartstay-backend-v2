package com.smartstay.smartstay.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

public class Utils {

    private static final String ALPHABETS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new Random();
    public static final String ENVIRONMENT_LOCAL = "LOCAL";
    public static final String ENVIRONMENT_DEV = "DEV";
    public static final String ENVIRONMENT_QA = "QA";
    public static final String ENVIRONMENT_PROD = "PROD";

    public static final String USER_INPUT_DATE_FORMAT = "dd-MM-yyyy";

    public static final String OUTPUT_DATE_FORMAT = "dd/MM/yyyy";
    public static final String OUTPUT_TIME_FORMAT = "hh:mm:ss aa";
    public static final String OUTPUT_MONTH_FORMAT = "MMM YYYY";
    public static final String OUTPUT_DATE_MONTH_FORMAT = "dd MMM";

    public static final String DATE_FORMAT_YY = "yyyy/MM/dd";
    public static final String DATE_FORMAT_ZOHO = "yyyy-MM-dd";

    public static final String ACCESS_RESTRICTED = "Access Restricted";
    public static final String UN_AUTHORIZED = "Unauthorized";
    public static final String INVALID = "Invalid";
    public static final String INVALID_EMAIL = "Invalid Email";
    public static final String INVALID_VENDOR = "Invalid Vendor";
    public static final String INVALID_BANKING = "Invalid Banking";
    public static final String INVALID_BANKING_DETAILS = "Bank details not found. Please add bank details first.";
    public static final String INVALID_ASSET = "Invalid Asset";

    public static final String INVALID_AMENITY = "Invalid Amenity";
    public static final String AMENITY_ALREADY_DELETED = "Amenity already deleted";


    public static final String INVALID_FLOOR = "Invalid Floor";
    public static final String INVALID_USER = "Invalid User";
    public static final String INVALID_OTP = "Invalid Otp";
    public static final String OTP_EXPIRED = "Otp Expired";
    public static final String ASSET_NAME_ALREADY_EXISTS = "Asset name already exists";
    public static final String SERIAL_NUMBER_ALREADY_EXISTS = "Serial number already exists";
    public static final String PASSWORD_RESET_SUCCESS = "Password reset successfully.";
    public static final String PASSWORD_CHANGED_SUCCESS = "Password changed successfully";
    public static final String TEMPLATE_TYPE_NOT_FOUND = "Template type not found for given templateTypeId!";
    public static final String INVALID_JOINING_DATE = "Invalid Joining Date";
    public static final String ELECTRICITY_CONFIG_NOT_SET_UP = "Electricity configuration is not setup";
    public static final String ALREADY_READING_TAKEN_THIS_DATE = "Already Reading taken for this date";
    public static final String PREVIOUS_CURRENT_READING_NOT_MATCHING = "Previous reading and current readings are not matching";
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
    public static final String INVALID_ROOM_ID = "Invalid Room Id";
    public static final String ROLE_NAME_EXISTS = "Role name already exists";
    public static final String ACTIVE_USERS_FOUND = "Active users found";
    public static final String INVALID_CUSTOMER_ID = "Invalid customer id";
    public static final String INVALID_HOSTEL_ID = "Invalid hostel id";
    public static final String INVALID_TRANSACTION_ID = "Invalid transaction id";
    public static final String INVALID_INVOICE_ID = "Invalid invoice id";
    public static final String INVALID_INVOICE_DATE = "Invalid invoice date";
    public static final String PAYMENT_SUCCESS = "Payment Success";
    public static final String CUSTOMER_ON_NOTICE = "Customer is already on notice";
    public static final String CUSTOMER_BOOKING_NOT_FOUND = "Booking not found for this customer";
    public static final String RESTRICTED_HOSTEL_ACCESS = "Do not have the access to access this hostel";
    public static final String N0_FLOOR_FOUND_HOSTEL = "No floor found for the specified hostel.";
    public static final String N0_ROOM_FOUND_FLOOR = "No room found for the specified floor.";
    public static final String NO_ROOM_FOUND_HOSTEL = "No room found for specified hostel";
    public static final String N0_BED_FOUND_ROOM = "No bed found for the specified room.";
    public static final String BED_CURRENTLY_UNAVAILABLE = "Bed is unavailable";
    public static final String RECHECK_DATE_SHOULD_BE_GREATER_THAN_JOINING_DATE = "Recheck date should be greater than joining date";
    public static final String BED_UNAVAILABLE_DATE = "Bed is unavailable for selected date";
    public static final String CHECK_IN_FUTURE_DATE_ERROR = "Check in cannot be accept for future dates";
    public static final String OTP_SENT_SUCCESSFULLY = "OTP has been sent successfully.";
    public static final String INVALID_BOOKING_ID = "Invalid booking id";
    public static final String PAYLOADS_REQUIRED = "Payloads required";
    public static final String TRY_AGAIN = "Try Again";
    public static final String INVALID_BED_ID = "Invalid bed id passed";
    public static final String CANNOT_DELETE_DEFAULT_ROLES = "Cannot delete default roles";
    public static final String CANNOT_EDIT_DEFAULT_ROLES = "Cannot edit default roles";
    public static final String FUTURE_DATES_NOT_ALLOWED = "Future Dates are not allowed";
    public static final String ACCOUNT_NO_ALREAY_EXISTS = "Account number already exists";
    public static final String CASH_ACCOUNT_ALREAY_EXISTS = "Cash Account already exists";
    public static final String REQUIRED_TRANSACTION_MODE = "Transaction mode required";
    public static final String INVALID_BANK_ID = "Invalid bank id";
    public static final String AMOUNT_REQUIRED = "Amount required";
    public static final String NO_ACCOUNT_NO_FOUND = "No account number found";
    public static final String NO_FROM_ACCOUNT_NO_FOUND = "No 'from account' found for this hostel.";
    public static final String NO_TO_ACCOUNT_NO_FOUND = "No 'To account' found for this hostel.";
    public static final String INVALID_ACCOUNT_TYPE = "Invalid account type";
    public static final String INVALID_ACCOUNT_TYPE_FROM_ACC = "Invalid account type for From account";
    public static final String INVALID_ACCOUNT_TYPE_FROM_TO = "Invalid account type for To account";
    public static final String INVALID_TRANSACTION_TYPE_FROM_ACC = "This account does’t have Debit access";
    public static final String INVALID_TRANSACTION_TYPE = "This account does’t have Debit and Credit access";
    public static final String INSUFFICIENT_BALANCE = "Insufficient balance";
    public static final String INVALID_TRANSACTION_TYPE_TO_ACC = "This account does’t have Credit access";
    public static final String TEMPLATE_NOT_AVAILABLE = "Template not available for this hostel";
    public static final String BILLING_RULE_NOT_AVAILABLE = "Billing Rule not found";
    public static final String CUSTOMER_ALREADY_CHECKED_IN = "Customer is already checked in";
    public static final String CUSTOMER_ALREADY_BOOKED = "Customer is already Booked";
    public static final String ELECTICITY_PRICE_REQUIRED = "Electricity Price Required";
    public static final String CUSTOMER_CHECKED_IN_INACTIVE_STATUS = "Cannot changed status to inactive for checked in customers";
    public static final String CUSTOMER_INACTIVE_VACTED_ERROR = "Cannot change status to inactive for vacated customers";
    public static final String CUSTOMER_ALREADY_INACTIVE_ERROR = "Customer is already inactive";
    public static final String CANNOT_INACTIVE_ACTIVE_CUSTOMERS = "Customer is currently active";
    public static final String INVOICE_ALREADY_PRESENT = "Invoice already exists for this dates";
    public static final String CUSTOMER_ALREADY_VACATED = "Customer already vacated";
    public static final String CUSTOMER_NOT_CHECKED_IN_ERROR = "Customer is not checked in";
    public static final String CUSTOMER_NOT_CHECKED_IN_DATE = "Customer is not checked in on this date";
    public static final String INVOICE_NUMBER_ALREADY_REGISTERED = "Invoice number already exists";
    public static final String CUSTOMER_CHECKED_NOT_IN_NOTICE = "Customer is not on notice";
    public static final String CANNOT_ENABLE_HOSTEL_ROOM_READINGS = "Cannot enable hostel based and room based together";
    public static final String CATEGORY_NAME_CATEGORY_ID_ERROR = "Category name or Category id is required";
    public static final String CATEGORY_NAME_ALREADY_REGISTERED = "Category name is already exists";
    public static final String SUB_CATEGORY_NAME_ALREADY_REGISTERED = "Subcategory name is already exists";
    public static final String INSUFFICIENT_FUND_ERROR = "Insufficient funds";
    public static final String INVALID_CATEGORY_ID = "Invalid category id";
    public static final String SUB_CATEGORY_NAME_REQUIRED = "Sub category name required";
    public static final String SUB_CATEGORY_ID_REQUIRED = "Sub category id required";
    public static final String NO_BOOKING_INFORMATION_FOUND = "No booking information found";
    public static final String YOU_CANNOT_TRANSFER = "You cannot transfer funds to the same UPI account.";
    public static final String FINAL_SETTLEMENT_GENERATED = "Final settlement is already generated";
    public static final String FINAL_SETTLEMENT_NOT_GENERATED = "Final settlement is pending";
    public static final String FINAL_SETTLEMENT_NOT_PAID = "Final Settlement is not fully paid";
    public static final String CHANGE_BED_SAME_BED_ERROR = "Customer is currently staying on the same bed";
    public static final String CHANGE_BED_SAME_DAY_ERROR = "Cannot change the bed on the same day customer is joined";
    public static final String CUSTOMER_VERIFIED_KYC = "Customer is already verified";
    public static final String INVALID_STARTING_DATE = "Invalid starting date";
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
    public static final String NO_RECORDS_FOUND = "No records found";
    public static final String CUSTOMER_NOT_CHECKED_IN = "Start date cannot be before joining date";
    public static final String REFUND_COMPLETED = "Refund already completed";
    public static final String CANNOT_REFUND_CANCELLED_INVOICE = "Cannot refund for cancelled invoices";
    public static final String CANNOT_REFUND_FOR_OLD_INVOICES = "Cannot refund for old invoices";
    public static final String CANNOT_REFUND_FOR_UNPAID_INVOICES = "Cannot refund for unpaid invoices";
    public static final String CANNOT_INITIATE_REFUND = "Cannot initiate refund";
    public static final String REFUND_PROCESSED_SUCCESSFULLY = "Refund process successfully";


    //Date validation messages
    public static final String REQUEST_DATE_MUST_AFTER_JOINING_DATE = "Request date must be after joining date.";
    public static final String CHECKOUT_DATE_MUST_AFTER_REQUEST_DATE = "Checkout date must be after request date.";

    public static final String REQUEST_DATE_MUST_AFTER_BILLING_START_DATE = "Request date must be after current billing cycle start date: ";
    public static final String CHECKOUT_DATE_MUST_AFTER_JOINING_DATE = "Checkout date must be after joining date.";






    public static final String AMENITY_ALREADY_EXIST = "Amenity with the same name already exists in this hostel.";


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

    public static String dateToDateMonth(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(OUTPUT_DATE_MONTH_FORMAT).format(date);
    }

    public static String dateToMonth(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(OUTPUT_MONTH_FORMAT).format(date);
    }

    public static String dateToTime(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(OUTPUT_TIME_FORMAT).format(date);
    }


    public static boolean compareWithTodayDate(Date date2) {
        String dateString = new SimpleDateFormat(OUTPUT_DATE_FORMAT).format(new Date());
        Date today;
        try {
            today = new SimpleDateFormat(OUTPUT_DATE_FORMAT).parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date");
        }

        return today.before(date2) || today.compareTo(date2) <= 0;
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
        LocalDate localDate1 = date1.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate localDate2 = date2.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return localDate1.compareTo(localDate2);
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

        if (data instanceof Long) {
            if ((Long) data == 0) {
                return false;
            }
        }
        return true;
    }

    public static long findNumberOfDays(Date date1, Date date2) {
        LocalDate start = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end   = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return ChronoUnit.DAYS.between(start, end) + 1;
    }

    public static long findNoOfDaysLeftInCurrentMonth(Date date) {
        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate lastDayOfMonth = localDate.withDayOfMonth(localDate.lengthOfMonth());
        return ChronoUnit.DAYS.between(localDate, lastDayOfMonth) + 1;
    }

    public static long findNoOfDaysInCurrentMonth(Date date) {
        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return localDate.lengthOfMonth();
    }

    public static Date addDaysToDate(Date date, int noOfDays) {
        return Date.from(date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .plusDays(noOfDays)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static final int findDateFromDate(Date date) {
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            return cal.get(Calendar.DAY_OF_MONTH);
        }
        return 0;
    }

    public static final Date findLastDate(Integer cycleStartDay, Date date) {
        LocalDate today = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();;
        LocalDate startDate = LocalDate.of(today.getYear(), today.getMonth(), cycleStartDay);

        LocalDate cycleEnd;
        if (cycleStartDay == 1) {
            YearMonth ym = YearMonth.from(startDate);
            cycleEnd = ym.atEndOfMonth();
        } else {
            LocalDate nextMonth = startDate.plusMonths(1);
            int endDay = cycleStartDay - 1;

            int lastDayOfNextMonth = YearMonth.from(nextMonth).lengthOfMonth();
            if (endDay > lastDayOfNextMonth) {
                endDay = lastDayOfNextMonth;
            }

            cycleEnd = nextMonth.withDayOfMonth(endDay);
        }

        return Date.from(cycleEnd.atStartOfDay(ZoneId.systemDefault()).toInstant());

    }


    public static int generateExpenseNumber() {
        Random random = new Random();
        int number = 10000000 + random.nextInt(90000000);

        return number;
    }

    public static String formMessageWithDate(Date date, String message) {
        return message + " " + dateToString(date);
    }

    public static String generateReference() {
        StringBuilder ref = new StringBuilder();

        // 4 letters
        for (int i = 0; i < 4; i++) {
            ref.append(ALPHABETS.charAt(RANDOM.nextInt(ALPHABETS.length())));
        }
        ref.append("-");

        // 4 digits
        for (int i = 0; i < 4; i++) {
            ref.append(RANDOM.nextInt(10));
        }
        ref.append("-");

        // 4 alphanumeric
        for (int i = 0; i < 4; i++) {
            ref.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }

        return ref.toString();
    }

    public static String formPrefixSuffix(String invoiceNumber) {
        StringBuilder prefixSuffix = new StringBuilder();
        String[] invoicePrefixSuffix = invoiceNumber.split("-");
        if (invoicePrefixSuffix.length > 0) {
            prefixSuffix.append(invoicePrefixSuffix[0]);
            if (invoicePrefixSuffix.length > 1) {
               if (invoicePrefixSuffix.length > 2) {
                   for (int i=0; i<invoicePrefixSuffix.length-2; i++) {
                       prefixSuffix.append("-");
                       prefixSuffix.append(invoicePrefixSuffix[i]);
                   }
               }
               prefixSuffix.append("-");
               int lastNumber = Integer.parseInt(invoicePrefixSuffix[invoicePrefixSuffix.length - 1]) + 1;
               if (lastNumber < 10) {
                   prefixSuffix.append("00");
                   prefixSuffix.append(lastNumber);
               }
               else if (lastNumber < 100) {
                    prefixSuffix.append("0");
                    prefixSuffix.append(lastNumber);
                }
                else {
                   prefixSuffix.append(lastNumber);
               }
            }
        }

        return prefixSuffix.toString();
    }

    public static Date formDateFromDay(int day, Date currentDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        return calendar.getTime();
    }

    public static String capitalize(String value) {
        if (value == null || value.isEmpty()) return value;
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

}
