package com.smartstay.smartstay.util.columnOptions;

import com.smartstay.smartstay.dao.ColumnFilters;

import java.util.List;
import java.util.Comparator;
import java.util.Objects;

public class ReceiptColumnUtils {
    private ReceiptColumnUtils() {
    }

    public static List<ColumnFilters> validColumns(List<ColumnFilters> columns) {
        if (columns == null) {
            return List.of();
        }
        return columns.stream()
                .filter(Objects::nonNull)
                .filter(column -> column.getFieldName() != null && !column.getFieldName().isBlank())
                .sorted(Comparator.comparingInt(ColumnFilters::getOrder))
                .toList();
    }

    public static List<ColumnFilters> defaultColumns() {
        return List.of(
                new ColumnFilters(1, "Receipt No", true),
                new ColumnFilters(2, "Full Name", true),
                new ColumnFilters(3, "Reference ID", true),
                new ColumnFilters(4, "Invoice Number", true),
                new ColumnFilters(5, "Payment Date", true),
                new ColumnFilters(6, "Type", true),
                new ColumnFilters(7, "Payment Mode", true),
                new ColumnFilters(8, "Amount", true));
    }
}
