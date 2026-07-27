package com.smartstay.smartstay.ennum;

public enum QrType {
    UPI,
    CARD;

    public static QrType fromValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        for (QrType type : values()) {
            if (type.name().equalsIgnoreCase(trimmed)) {
                return type;
            }
        }
        return null;
    }
}
