package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.responses.Hostels;
import com.smartstay.smartstay.responses.hostel.HostelsList;

import java.util.function.Function;

public class HostelListMapper implements Function<Hostels, HostelsList> {

    private int noOfFloors = 0;
    private int noOfRooms = 0;
    private int noOfBeds = 0;
    private int noOfOccupiedBeds = 0;
    private int noOfAvailableBeds = 0;

    public HostelListMapper(int noOfFloors, int noOfRooms, int noOfBeds, int noOfOccupiedBeds, int noOfAvailableBeds) {
        this.noOfFloors = noOfFloors;
        this.noOfRooms = noOfRooms;
        this.noOfBeds = noOfBeds;
        this.noOfOccupiedBeds = noOfOccupiedBeds;
        this.noOfAvailableBeds = noOfAvailableBeds;
    }

    @Override
    public HostelsList apply(Hostels hostels) {
        HostelsList list = new HostelsList();
        list.setHostelId(hostels.hostelId());
        list.setLandmark(hostels.landmark());
        list.setCountry(hostels.country());
        list.setCity(hostels.city());
        list.setHouseNo(hostels.houseNo());
        list.setEmailId(hostels.emailId());
        list.setLastUpdate(hostels.lastUpdate());
        list.setName(hostels.name());
        list.setMessage(hostels.message());
        list.setMobile(hostels.mobile());
        list.setState(hostels.state());
        list.setPincode(hostels.pincode());
        list.setMainImage(hostels.mainImage());
        list.setSubscriptionEndDate(hostels.subscriptionEndDate());
        list.setSubscriptionValid(hostels.isSubscriptionValid());
        list.setStreet(hostels.street());
        list.setMessage(hostels.message());
        list.setNoOfFloors(noOfFloors);
        list.setNoOfRooms(noOfRooms);
        list.setNoOfBeds(noOfBeds);
        list.setNoOfAvailableBeds(noOfAvailableBeds);
        list.setNoOfOccupiedBeds(noOfOccupiedBeds);
        return list;
    }
}
