package com.smartstay.smartstay.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class NameUtils {

    public static String getFullName(String firstName, String lastName) {
        StringBuilder builder = new StringBuilder();
        if (firstName != null) {
            builder.append(firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            builder.append(" ");
            builder.append(lastName);
        }

        return builder.toString();
    }

    public static String getInitials(String firstName, String lastName) {
        StringBuilder initials = new StringBuilder();

        boolean hasFirstName = firstName != null && !firstName.trim().isEmpty();
        boolean hasLastName = lastName != null && !lastName.trim().isEmpty();

        // Nothing to derive initials from (e.g. an incomplete draft customer).
        if (!hasFirstName && !hasLastName) {
            return "";
        }

        if (hasFirstName) {
            initials.append(firstName.trim().toUpperCase().charAt(0));
        }
        if (hasLastName) {
            initials.append(lastName.trim().toUpperCase().charAt(0));
        }
        else {
            // No last name: derive the second initial from the first name.
            String fname = firstName.trim();
            String[] fnameArr = fname.split(" ");
            if (fnameArr.length > 1) {
                String lastString = fnameArr[fnameArr.length - 1];
                initials.append(lastString.toUpperCase().charAt(0));
            }
            else if (fname.length() > 1) {
                initials.append(fname.toUpperCase().charAt(1));
            }
            else {
                initials.append(fname.toUpperCase().charAt(0));
            }
        }

        return initials.toString();
    }

    public static String capitalizeWordsWithUnderscore(String input) {

        if (input == null || input.isBlank()) {
            return input;
        }

        String[] words = input.toLowerCase()
                .replace("_", " ")
                .split("\\s+");

        return Arrays.stream(words)
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
