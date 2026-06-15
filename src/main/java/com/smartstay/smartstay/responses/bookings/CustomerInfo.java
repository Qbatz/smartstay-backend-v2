package com.smartstay.smartstay.responses.bookings;

public record CustomerInfo(String customerId, String fullName, String profilePic, String initials, String firstName,
                           String lastName, String floorName, String bedName, String roomName) {
}
