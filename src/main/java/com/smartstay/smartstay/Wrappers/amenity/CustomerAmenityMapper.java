package com.smartstay.smartstay.Wrappers.amenity;

import com.smartstay.smartstay.dao.AmenitiesV1;
import com.smartstay.smartstay.dao.CustomersAmenity;
import com.smartstay.smartstay.responses.customer.Amenities;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class CustomerAmenityMapper implements Function<CustomersAmenity, Amenities> {

    private List<AmenitiesV1> listAmenities = null;

    public CustomerAmenityMapper(List<AmenitiesV1> listAmenities) {
        this.listAmenities = listAmenities;
    }

    @Override
    public Amenities apply(CustomersAmenity customersAmenity) {
        AmenitiesV1 amenitiesV1 = listAmenities
                .stream()
                .filter(i -> i.getAmenityId().equalsIgnoreCase(customersAmenity.getAmenityId()))
                .findFirst()
                .orElse(null);
        String amenityName = null;
        if (amenitiesV1 != null) {
            amenityName = amenitiesV1.getAmenityName();
        }


        return new Amenities(customersAmenity.getAmenityId(),
                amenityName,
                customersAmenity.getAmenityPrice(),
                Utils.dateToString(customersAmenity.getStartDate()),
                Utils.dateToString(customersAmenity.getEndDate()));
    }
}
