package com.smartstay.smartstay.responses.filterOptions;

public record ColumnFiltersRes(int order,
                               String fieldName,
                               boolean isSelected,
                               boolean isLocked) {
}
