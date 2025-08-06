package com.smartstay.smartstay.responses.hostel;

import java.util.List;

public record HostelDetails(String hostelId, String mainImage, String city, String country, String emailId, String name,
                            String houseNo, String landmark, String mobile, int pinCode, String state, String street,
                            String lastUpdate, boolean isSubscriptionActive, String nextBillingDate,
                            int remainingDaysLeft, int numberOfFloors, List<FloorDetails> floorDetails) {
}
