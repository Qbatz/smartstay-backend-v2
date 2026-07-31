package com.smartstay.smartstay.ennum;

public enum DateFilter {
    ALL,
    THIS_MONTH,
    LAST_3_MONTHS,
    LAST_6_MONTHS,
    CUSTOM;

    public static DateFilter fromValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        for (DateFilter filter : values()) {
            if (filter.name().equalsIgnoreCase(trimmed)) {
                return filter;
            }
        }
        return null;
    }
}
