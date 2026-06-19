package com.smartstay.smartstay.util.columnOptions;

import com.smartstay.smartstay.dao.ColumnFilters;

import java.util.List;

public class ExpenseColumnUtils {
    public static final String EXPENSE_NO = "Expense No";
    public static final String TITLE = "Title";
    public static final String DATE = "Date";
    public static final String CATEGORY = "Category";
    public static final String SUB_CATEGORY = "Sub Category";
    public static final String VENDOR = "Vendor";
    public static final String STATUS = "Status";
    public static final String PAYMENT_MODE = "Payment Mode";
    public static final String TOTAL_AMOUNT = "Total Amount";
    public static final String PAID_AMOUNT = "Paid Amount";
    public static final String BALANCE_AMOUNT = "Balance Amount";

    private ExpenseColumnUtils() {
    }

    /**
     * Default, ordered column configuration for the expense listing screen. Used when the user has
     * not stored a preference in the {@code table_columns} table for {@code MODULE_EXPENSE}.
     */
    public static List<ColumnFilters> defaultColumns() {
        return List.of(
                new ColumnFilters(1, EXPENSE_NO, true),
                new ColumnFilters(2, TITLE, true),
                new ColumnFilters(3, DATE, true),
                new ColumnFilters(4, CATEGORY, true),
                new ColumnFilters(5, SUB_CATEGORY, true),
                new ColumnFilters(6, VENDOR, true),
                new ColumnFilters(7, STATUS, true),
                new ColumnFilters(8, PAYMENT_MODE, true),
                new ColumnFilters(9, TOTAL_AMOUNT, true),
                new ColumnFilters(10, PAID_AMOUNT, true),
                new ColumnFilters(11, BALANCE_AMOUNT, true));
    }
}
