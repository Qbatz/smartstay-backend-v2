package com.smartstay.smartstay.responses.InvoiceRedemption;

public record CustomerInfo(String firstName,
                           String lastName,
                           String fullName,
                           String initials,
                           String profilePic,
                           String bedName,
                           String roomName,
                           String floorName) {
}
