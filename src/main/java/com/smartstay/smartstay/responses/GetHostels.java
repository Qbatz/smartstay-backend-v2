package com.smartstay.smartstay.responses;

public record GetHostels(String hostelId, String hostelName, String city, String country, String emailId, String name, String houseNo, String landmark, String mobile, int pincode, String state, String street, String lastUpdate, String subscriptionEndDate, boolean isSubscriptionValid, String message) {
}
