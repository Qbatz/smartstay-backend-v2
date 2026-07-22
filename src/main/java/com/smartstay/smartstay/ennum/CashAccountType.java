package com.smartstay.smartstay.ennum;

public enum CashAccountType {
    PETTY_CASH("Petty Cash"),
    OFFICE_CASH("Office Cash");

    private final String value;

    CashAccountType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    public static CashAccountType fromValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        for (CashAccountType type : values()) {
            if (type.name().equalsIgnoreCase(trimmed) || type.value.equalsIgnoreCase(trimmed)) {
                return type;
            }
        }
        return null;
    }
}
