package com.smartstay.smartstay.Wrappers.amenity;

import com.smartstay.smartstay.dao.AmenitiesV1;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.CustomersAmenity;
import com.smartstay.smartstay.ennum.CustomerStatus;
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
                    StringBuilder initials = new StringBuilder();

                    if (i.getFirstName() != null) {
                        initials.append(i.getFirstName().toUpperCase().charAt(0));
                        fullName.append(i.getFirstName());
                    }
                    if (i.getLastName() != null && !i.getLastName().equalsIgnoreCase("")) {
                        fullName.append(" ");
                        fullName.append(i.getLastName());
                        initials.append(i.getLastName().toUpperCase().charAt(0));
                    }
                    else {
                        if (i.getFirstName() != null && i.getFirstName().length() > 1) {
                            initials.append(i.getFirstName().toLowerCase().charAt(1));
                        }
                    }

                    return new CustomerResponse(i.getCustomerId(), fullName.toString(), initials.toString(), i.getProfilePic(), false);
                })
                .toList();

        List<CustomerResponse> unassigned = listCustomers
                .stream()
                .filter(i -> unique.contains(i.getCustomerId()))
                .map(i -> {
                    boolean canAssign = true;

                    StringBuilder fullName = new StringBuilder();
                    StringBuilder initials = new StringBuilder();

                    if (i.getFirstName() != null) {
                        initials.append(i.getFirstName().toUpperCase().charAt(0));
                        fullName.append(i.getFirstName());
                    }
                    if (i.getLastName() != null && !i.getLastName().trim().equalsIgnoreCase("")) {
                        fullName.append(" ");
                        fullName.append(i.getLastName());
                        initials.append(i.getLastName().toUpperCase().charAt(0));
                    }
                    else {
                        if (i.getFirstName() != null && i.getFirstName().length() > 1) {
                            initials.append(i.getFirstName().toLowerCase().charAt(1));
                        }
                    }


                    if (i.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())) {
                        canAssign = false;
                    }
                    return new CustomerResponse(i.getCustomerId(), fullName.toString(), initials.toString(), i.getProfilePic(), canAssign);
                })
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
