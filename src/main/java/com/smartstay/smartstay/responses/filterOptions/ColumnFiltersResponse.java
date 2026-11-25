package com.smartstay.smartstay.responses.filterOptions;

public record ColumnFiltersResponse(int order,
                                    String fieldName,
                                    boolean isSelected,
                                    boolean isLocked) {
}
