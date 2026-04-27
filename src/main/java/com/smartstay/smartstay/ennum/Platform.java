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
}
