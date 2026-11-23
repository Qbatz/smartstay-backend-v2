package com.smartstay.smartstay.util.columnOptions;

import com.smartstay.smartstay.dao.ColumnFilters;

import java.util.List;

public class InvRegisterConsolidatedColumnUtils {

    public static final String FLOOR = "Floor";
    public static final String ROOM = "Room";
    public static final String BED = "Bed";
    public static final String TENANT_NAME = "Tenant Name";
    public static final String CONTACT_NUMBER = "Contact Number";
    public static final String ACTUAL_RENT = "Actual Rent";
    public static final String PENDING_RENT = "Pending Rent";
    public static final String EB_AMOUNT = "Eb Amount";
    public static final String PENDING_ADVANCE = "Pending Advance";
    public static final String PENDING_NON_REFUNDABLE = "Pending Non-Refundable";
    public static final String TOTAL_PENDING_AMOUNT = "Total Pending Amount";
    public static final String INVOICES = "Invoices";

    public static List<ColumnFilters> defaultColumns() {
        return List.of(
                new ColumnFilters(1, FLOOR, true),
                new ColumnFilters(2, ROOM, true),
                new ColumnFilters(3, BED, true),
                new ColumnFilters(4, TENANT_NAME, true),
                new ColumnFilters(5, CONTACT_NUMBER, true),
                new ColumnFilters(6, ACTUAL_RENT, true),
                new ColumnFilters(7, PENDING_RENT, true),
                new ColumnFilters(8, EB_AMOUNT, true),
                new ColumnFilters(9, PENDING_ADVANCE, true),
                new ColumnFilters(10, PENDING_NON_REFUNDABLE, true),
                new ColumnFilters(11, TOTAL_PENDING_AMOUNT, true),
                new ColumnFilters(12, INVOICES, true));
    }
}
