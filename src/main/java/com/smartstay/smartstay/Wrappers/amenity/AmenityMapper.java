package com.smartstay.smartstay.Wrappers.amenity;

import com.smartstay.smartstay.dao.AmenitiesV1;
import com.smartstay.smartstay.repositories.CustomerAmenityRepository;
import com.smartstay.smartstay.responses.amenitity.AmenityInfoProjection;
import com.smartstay.smartstay.responses.amenitity.AmenityResponse;
import com.smartstay.smartstay.responses.amenitity.CustomerData;
import com.smartstay.smartstay.responses.amenitity.CustomerResponse;

import java.util.List;
import java.util.function.Function;

public class AmenityMapper implements Function<AmenityInfoProjection, AmenityResponse> {

    private final CustomerAmenityRepository customerAmenityRepository;
    private String hostelId = "";

    public AmenityMapper(CustomerAmenityRepository customerAmenityRepository, String hostelId) {
        this.customerAmenityRepository = customerAmenityRepository;
        this.hostelId = hostelId;
    }
    @Override
    public AmenityResponse apply(AmenityInfoProjection amenity) {
        List<CustomerData> customers =
                customerAmenityRepository.findCustomersWithAmenityStatus(
                        amenity.getAmenityId(),
                        hostelId
                );

        List<CustomerResponse> assigned = customers.stream()
                .filter(c -> "ASSIGNED".equalsIgnoreCase(c.getStatus()))
                .map(c -> new CustomerResponse(c.getCustomerId(), c.getCustomerName()))
                .toList();

        List<CustomerResponse> unassigned = customers.stream()
                .filter(c -> "UNASSIGNED".equalsIgnoreCase(c.getStatus()))
                .map(c -> new CustomerResponse(c.getCustomerId(), c.getCustomerName()))
                .toList();

        return new AmenityResponse(
                amenity.getAmenityId(),
                amenity.getAmenityName(),
                amenity.getAmenityAmount(),
                amenity.getProRate() != null && amenity.getProRate(),
                assigned,
                unassigned
        );

    }

}
