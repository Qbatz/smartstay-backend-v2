package com.smartstay.smartstay.util.columnOptions;

import com.smartstay.smartstay.dao.ColumnFilters;

import java.util.List;

public class VendorColumnUtils {
    public static final String PROFILE_PIC = "Profile Pic";
    public static final String FULL_NAME = "Full Name";
    public static final String JOINING_DATE = "Joining Date";
    public static final String MOBILE_NUMBER = "Mobile No";
    public static final String EMAIL_ID = "Email ID";
    public static final String VENDOR_CODE = "Vendor Code";
    public static final String VENDOR_CATEGORY = "Vendor Category";
    public static final String CREDIT_LIMIT = "Credit Limit";
    public static final String CREDIT_PERIOD = "Credit Period";
    public static final String OUTSTANDING = "Outstanding";
    public static final String LAST_TRANSACTION = "Last Transaction";
    public static final String PAYMENT_STATUS = "Payment Status";

    private VendorColumnUtils() {
    }

    /**
     * Default, ordered column configuration for the vendor listing screen. Used when the user
     * has not stored a preference in the {@code table_columns} table and when no DB-level
     * filter option seed exists for {@code MODULE_VENDOR}.
     */
    public static List<ColumnFilters> defaultColumns() {
        return List.of(
                new ColumnFilters(1, PROFILE_PIC, true),
                new ColumnFilters(2, FULL_NAME, true),
                new ColumnFilters(3, JOINING_DATE, true),
                new ColumnFilters(4, MOBILE_NUMBER, true),
                new ColumnFilters(5, EMAIL_ID, true),
                new ColumnFilters(6, VENDOR_CODE, true),
                new ColumnFilters(7, VENDOR_CATEGORY, true),
                new ColumnFilters(8, CREDIT_LIMIT, true),
                new ColumnFilters(9, CREDIT_PERIOD, true),
                new ColumnFilters(10, OUTSTANDING, true),
                new ColumnFilters(11, LAST_TRANSACTION, true),
                new ColumnFilters(12, PAYMENT_STATUS, true));
    }
}
