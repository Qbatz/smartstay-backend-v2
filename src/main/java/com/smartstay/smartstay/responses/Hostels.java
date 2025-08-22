package com.smartstay.smartstay.responses;


import com.smartstay.smartstay.dao.Subscription;
import com.smartstay.smartstay.responses.hostel.HostelImages;

import java.util.List;

public record Hostels(String hostelId,
                      String mainImage,
                      String city,
                      String country,
                      String emailId,
                      String name,
                      String houseNo,
                      String landmark,
                      String mobile,
                      int pincode,
                      String state,
                      String street,
                      String lastUpdate,
                      String subscriptionEndDate, boolean isSubscriptionValid,
                      String message, int noOfFloors, int noOfRooms, int noOfBeds,
                      int noOfOccupiedBeds, int noOfAvailableBeds,
                      List<HostelImages> images,
                      String initials) {
}
