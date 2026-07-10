package com.smartstay.smartstay.Wrappers.amenity;

import com.smartstay.smartstay.dao.AmenitiesV1;
import com.smartstay.smartstay.dto.amenity.AmenityRequestDTO;
import com.smartstay.smartstay.responses.customer.AvailableAmenities;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class AvailableAmenitiesMapper implements Function<AmenitiesV1, AvailableAmenities> {

    List<AmenityRequestDTO> requestedAmenityList = null;

    public AvailableAmenitiesMapper(List<AmenityRequestDTO> requestedAmenity) {
        this.requestedAmenityList = requestedAmenity;
    }

    @Override
    public AvailableAmenities apply(AmenitiesV1 amenitiesV1) {
        boolean isRequested = false;

        if (requestedAmenityList != null) {
            AmenityRequestDTO requestedAmenity = requestedAmenityList.stream()
                    .filter(i -> i.amenityId().equalsIgnoreCase(amenitiesV1.getAmenityId()))
                    .findFirst()
                    .orElse(null);
            if (requestedAmenity != null) {
                isRequested = true;
            }
        }
        return new AvailableAmenities(amenitiesV1.getAmenityName(),
                amenitiesV1.getAmenityId(),
                Utils.roundOffWithTwoDigit(amenitiesV1.getAmenityAmount()),
                isRequested);
    }
}
