package com.smartstay.smartstay.Wrappers.amenity;

import com.smartstay.smartstay.dao.AmenitiesV1;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.CustomersAmenity;
import com.smartstay.smartstay.repositories.CustomerAmenityRepository;
import com.smartstay.smartstay.responses.amenitity.AmenityInfoProjection;
import com.smartstay.smartstay.responses.amenitity.AmenityResponse;
import com.smartstay.smartstay.responses.amenitity.CustomerData;
import com.smartstay.smartstay.responses.amenitity.CustomerResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class AmenityMapper implements Function<AmenityInfoProjection, AmenityResponse> {

    private List<Customers> listCustomers = null;
    private List<CustomersAmenity> listCustomersAmenity = null;

    public AmenityMapper(List<Customers> listCustomers, List<CustomersAmenity> listCustomersAmenity) {
        this.listCustomers = listCustomers;
        this.listCustomersAmenity = listCustomersAmenity;
    }
    @Override
    public AmenityResponse apply(AmenityInfoProjection amenity) {
        List<String> listCustomerIds = listCustomers
                .stream()
                .map(Customers::getCustomerId)
                .toList();
        List<String> assignedCustomers = listCustomersAmenity
                .stream()
                .map(CustomersAmenity::getCustomerId)
                .toList();
        Set<String> totalCustomerIds = new HashSet<>(listCustomerIds);
        Set<String> assignedCustomerIds = new HashSet<>(assignedCustomers);

        Set<String> common = new HashSet<>(totalCustomerIds);
        common.retainAll(assignedCustomerIds);


        Set<String> unique = new HashSet<>(totalCustomerIds);
        unique.removeAll(common);



        List<CustomerResponse> assigned = listCustomers
                .stream()
                .filter(i -> common.contains(i.getCustomerId()))
                .map(i -> {
                    StringBuilder fullName = new StringBuilder();
                    if (i.getFirstName() != null) {
                        fullName.append(i.getFirstName());
                    }
                    if (i.getLastName() != null && !i.getLastName().equalsIgnoreCase("")) {
                        fullName.append(" ");
                        fullName.append(i.getLastName());
                    }

                    return new CustomerResponse(i.getCustomerId(), fullName.toString());
                })
                .toList();

        List<CustomerResponse> unassigned = listCustomers
                .stream()
                .filter(i -> unique.contains(i.getCustomerId()))
                .map(i -> new CustomerResponse(i.getCustomerId(), i.getFirstName()))
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
