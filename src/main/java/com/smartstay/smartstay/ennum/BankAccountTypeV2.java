package com.smartstay.smartstay.ennum;


public enum BankAccountTypeV2 {
    BANK,
    CASH;

    public static BankAccountTypeV2 fromValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        for (BankAccountTypeV2 type : values()) {
            if (type.name().equalsIgnoreCase(trimmed)) {
                return type;
            }
        }
        return null;
    }
}
