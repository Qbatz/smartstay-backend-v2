package com.smartstay.smartstay.responses;


import com.smartstay.smartstay.dao.Subscription;

import java.util.List;

public record Hostels(String hostelId, String mainImage, String city, String country, String emailId, String name, String houseNo, String landmark, String mobile, int pincode, String state, String street, String lastUpdate, String subscriptionEndDate, boolean isSubscriptionValid, String message) {
}
