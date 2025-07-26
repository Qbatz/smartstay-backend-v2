package com.smartstay.smartstay.responses.bookings;

public record Bookings(String bookingId, String customerId, String joiningDate, Double rentAmount, String hostelId, String firstName, String city, String state, Long country, String currentStatus, String emailId, String profilePic) {

}
