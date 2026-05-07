package com.smartstay.smartstay.responses.bookings;

public record AdvanceListItems(
        String invoiceId,
        String invoiceNumber,
        String invoiceType,
        Double invoiceAmount,
        Double availableAmount,
        String invoiceDate,
        String bookingDate,
        String firstName,
        String lastName,
        String fullName,
        String initials,
        String profilePic,
        String mobileNumber,
        String floorName,
        String bedName,
        String roomName) {
}
