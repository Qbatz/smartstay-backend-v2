package com.smartstay.smartstay.responses.hostel;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class HostelsList {
    private String hostelId= null;
    private String mainImage= null;
    private String city= null;
    private String country= null;
    private String emailId= null;
    private String name= null;
    private String houseNo= null;
    private String landmark= null;
    private String mobile= null;
    private int pincode= 0;
    private String state= null;
    private String street= null;
    private String lastUpdate= null;
    private String subscriptionEndDate= null;
    private boolean isSubscriptionValid= false;
    private String message= null;
    private int noOfFloors = 0;
    private int noOfRooms = 0;
    private int noOfBeds = 0;
    private int noOfOccupiedBeds = 0;
    private int noOfAvailableBeds = 0;
}
