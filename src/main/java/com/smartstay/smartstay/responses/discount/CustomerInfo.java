package com.smartstay.smartstay.responses.discount;

public record CustomerInfo(String customerId,
                           String fullName,
                           String lastName,
                           String firstName,
                           String initials,
                           String profilePic) {
}
