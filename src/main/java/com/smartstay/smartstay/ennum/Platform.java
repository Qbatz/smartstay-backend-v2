package com.smartstay.smartstay.ennum;

public enum Platform {
    ANDROID,
    IOS;

    public static Platform fromValue(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Platform.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Platform resolveOrDefault(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ANDROID;
        }
        return fromValue(value);
    }
}
