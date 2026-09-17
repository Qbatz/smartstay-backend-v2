package com.smartstay.smartstay.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized address formatting so the same single-line address can be reused across APIs without
 * duplication. Joins the address parts with ", ", trimming each value and skipping any that are
 * null/blank (so no empty segments or doubled separators appear).
 */
public final class AddressUtils {

    private AddressUtils() {
    }

    public static String formatAddress(String houseNo, String area, String landMark, String city,
                                       Integer pinCode, String state) {
        List<String> parts = new ArrayList<>();
        appendIfPresent(parts, houseNo);
        appendIfPresent(parts, area);
        appendIfPresent(parts, landMark);
        appendIfPresent(parts, city);
        if (pinCode != null && pinCode != 0) {
            parts.add(String.valueOf(pinCode));
        }
        appendIfPresent(parts, state);
        return String.join(", ", parts);
    }

    public static String formatCustomerAddress(String houseNo, String street, String city, String state, int pincode) {
        List<String> parts = new ArrayList<>();
        appendIfPresent(parts, houseNo);
        appendIfPresent(parts, street);
        appendIfPresent(parts, city);
        boolean hasState = state != null && !state.trim().isEmpty();
        boolean hasPincode = pincode > 0;
        if (hasState && hasPincode) {
            parts.add(state.trim() + "-" + pincode);
        } else if (hasState) {
            parts.add(state.trim());
        } else if (hasPincode) {
            parts.add(String.valueOf(pincode));
        }
        return String.join(", ", parts);
    }

    private static void appendIfPresent(List<String> parts, String value) {
        if (value != null && !value.trim().isEmpty()) {
            parts.add(value.trim());
        }
    }
}
