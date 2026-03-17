package com.smartstay.smartstay.Wrappers.amenity;

import com.smartstay.smartstay.dao.AmenitiesV1;
import com.smartstay.smartstay.dao.AmenityRequest;
import com.smartstay.smartstay.dto.amenity.AmenityRequestDTO;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class AmenityRequestMapper implements Function<AmenityRequest, AmenityRequestDTO> {

    List<AmenitiesV1> amenitiesList = null;

    public AmenityRequestMapper(List<AmenitiesV1> amenitiesList) {
        this.amenitiesList = amenitiesList;
    }

    @Override
    public AmenityRequestDTO apply(AmenityRequest amenityRequest) {
        AmenitiesV1 amenitiesV1 = null;
        Double amenityPrice = 0.0;
        String amenityName = null;
        String typeOfCalculation = null;
        if (amenitiesList != null) {
            amenitiesV1 = amenitiesList.stream()
                    .filter(i -> i.getAmenityId().equalsIgnoreCase(amenityRequest.getAmenityId()))
                    .findFirst()
                    .orElse(null);
            if (amenitiesV1 != null) {
                amenityPrice = amenitiesV1.getAmenityAmount();
                amenityName = amenitiesV1.getAmenityName();
                if (amenitiesV1.getIsProRate()) {
                    typeOfCalculation = "Pro rate";
                }
                else {
                    typeOfCalculation = "Monthly";
                }
            }
        }
        return new AmenityRequestDTO(amenityRequest.getAmenityId(),
                amenityName,
                Utils.dateToString(amenityRequest.getRequestedDate()),
                amenityPrice, typeOfCalculation);
    }
}
