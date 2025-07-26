package com.smartstay.smartstay.dto;

import java.util.Date;

public record Bookings(String bookingId, String customerId, Date joiningDate, Double rentAmount, String hostelId, String firstName, String city, String state, Long country, String currentStatus, String emailId, String profilePic) {
}
