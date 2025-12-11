package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.HostelV1;
import com.smartstay.smartstay.dto.subscription.SubscriptionDto;
import com.smartstay.smartstay.responses.Hostels;
import com.smartstay.smartstay.responses.hostel.HostelImages;
import com.smartstay.smartstay.util.Utils;
import org.springframework.security.core.parameters.P;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class HostelsMapper implements Function<HostelV1, Hostels> {

    private int noOfFloors = 0;
    private int noOfRooms = 0;
    private int noOfBeds = 0;
    private int noOfOccupiedBeds = 0;
    private int noOfAvailableBeds = 0;
    private SubscriptionDto subscriptionDto;

    public HostelsMapper(int noOfFloors, int noOfRooms, int noOfBeds, int noOfOccupiedBeds, int noOfAvailableBeds, SubscriptionDto subscriptionDto) {
        this.noOfFloors = noOfFloors;
        this.noOfRooms = noOfRooms;
        this.noOfBeds = noOfBeds;
        this.noOfOccupiedBeds = noOfOccupiedBeds;
        this.noOfAvailableBeds = noOfAvailableBeds;
        this.subscriptionDto = subscriptionDto;
    }

    @Override
    public Hostels apply(HostelV1 hostelV1) {
        List<HostelImages> listImages = hostelV1.getAdditionalImages()
                .stream()
                .map(item -> new HostelImages(item.getImageUrl(), item.getId())).toList();
        String[] initialsArray = null;
        StringBuilder initials = new StringBuilder();
        if (hostelV1.getHostelName() != null) {
            initialsArray = hostelV1.getHostelName().split(" ");
            if (initialsArray.length > 1) {
                initials.append(initialsArray[0].toUpperCase().charAt(0));
                initials.append(initialsArray[initialsArray.length -1].toUpperCase().charAt(0));
            }
            else {
                initials.append(initialsArray[0].toUpperCase().charAt(0));
                initials.append(initialsArray[0].toUpperCase().charAt(1));
            }
        }


        String subscriptionEndDate = null;
        boolean isSubscriptionValid = false;
        String message = null;
        if (subscriptionDto != null) {
            subscriptionEndDate = Utils.dateToString(subscriptionDto.endDate());
            isSubscriptionValid = subscriptionDto.isValid();
            if (subscriptionDto.endsIn() < 28 && subscriptionDto.endsIn() > 0) {
                message = Utils.formMessageWithDate(subscriptionDto.endDate(), "Your subscription will ends in");
            }
            else if (subscriptionDto.endsIn() < 10 && subscriptionDto.endsIn() >= 0) {
                message = "Your subscription will end today";
            }
            else if (subscriptionDto.endsIn() < 0){
                message = "Your subscription ended";
            }
        }
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
                Utils.dateToString(hostelV1.getUpdatedAt()), //last update
                subscriptionEndDate, //subscription end date
                isSubscriptionValid, //isValid
                message, noOfFloors, noOfRooms , noOfBeds, noOfOccupiedBeds, noOfAvailableBeds,
                listImages, initials.toString());
    }
}
