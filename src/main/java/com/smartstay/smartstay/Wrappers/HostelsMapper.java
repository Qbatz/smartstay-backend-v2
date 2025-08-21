package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.HostelV1;
import com.smartstay.smartstay.responses.Hostels;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class HostelsMapper implements Function<HostelV1, Hostels> {

    private int noOfFloors = 0;
    private int noOfRooms = 0;
    private int noOfBeds = 0;
    private int noOfOccupiedBeds = 0;
    private int noOfAvailableBeds = 0;

    public HostelsMapper(int noOfFloors, int noOfRooms, int noOfBeds, int noOfOccupiedBeds, int noOfAvailableBeds) {
        this.noOfFloors = noOfFloors;
        this.noOfRooms = noOfRooms;
        this.noOfBeds = noOfBeds;
        this.noOfOccupiedBeds = noOfOccupiedBeds;
        this.noOfAvailableBeds = noOfAvailableBeds;
    }

    @Override
    public Hostels apply(HostelV1 hostelV1) {
        return new Hostels(hostelV1.getHostelId(),
                hostelV1.getMainImage(),
                hostelV1.getCity(),
                String.valueOf(hostelV1.getCountry()),
                hostelV1.getEmailId(),
                hostelV1.getHostelName(),
                hostelV1.getHouseNo(),
                hostelV1.getLandmark(),
                hostelV1.getMobile(),
                hostelV1.getPincode(),
                hostelV1.getState(),
                hostelV1.getStreet(),
                Utils.dateToString(hostelV1.getUpdatedAt()),
                Utils.dateToString(hostelV1.getSubscription().get(hostelV1.getSubscription().size()-1).getNextBillingAt()),
                Utils.compareWithTodayDate(hostelV1.getSubscription().get(hostelV1.getSubscription().size()-1).getNextBillingAt()),
                "", noOfFloors, noOfRooms , noOfBeds, noOfOccupiedBeds, noOfAvailableBeds);
    }
}
