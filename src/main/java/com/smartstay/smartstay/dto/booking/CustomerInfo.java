package com.smartstay.smartstay.dto.booking;

public record CustomerInfo(String firstName,
                           String lastName,
                           String fullName,
                           String customerId,
                           String profilePic,
                           String initials) {
}
