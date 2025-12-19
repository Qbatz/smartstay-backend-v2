package com.smartstay.smartstay.Wrappers.amenity;

import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.CustomersAmenity;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.ennum.CustomerStatus;
import com.smartstay.smartstay.responses.amenitity.AmenityInfoProjection;
import com.smartstay.smartstay.responses.amenitity.AmenityResponse;
import com.smartstay.smartstay.responses.amenitity.CustomerResponse;
import com.smartstay.smartstay.util.Utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class AmenityMapper implements Function<AmenityInfoProjection, AmenityResponse> {

    private List<Customers> listCustomers = null;
    private List<CustomersAmenity> listCustomersAmenity = null;
    private List<BedDetails> listBeds = null;
    private HashMap<String, Integer> bedCustomerMapper = null;

    public AmenityMapper(List<Customers> listCustomers, List<CustomersAmenity> listCustomersAmenity, List<BedDetails> listBeds, HashMap<String, Integer> mapper) {
        this.listCustomers = listCustomers;
        this.listCustomersAmenity = listCustomersAmenity;
        this.listBeds = listBeds;
        this.bedCustomerMapper = mapper;
    }
    @Override
    public AmenityResponse apply(AmenityInfoProjection amenity) {
        AtomicReference<String> bedName = new AtomicReference<>("NA");
        AtomicReference<String> floorName = new AtomicReference<>("NA");
        AtomicReference<String> roomName = new AtomicReference<>("NA");
        AtomicBoolean isEnding = new AtomicBoolean(false);
        AtomicReference<String> endDate = new AtomicReference<>("");
        List<String> listCustomerIds = listCustomers
                .stream()
                .map(Customers::getCustomerId)
                .toList();
        List<String> assignedCustomers = listCustomersAmenity
                .stream()
                .filter(i -> {
                    if (i.getEndDate() == null){
                        return true;
                    }
                    if (Utils.compareWithTwoDates(i.getEndDate(), new Date()) < 0) {
                        return false;
                    }
                    return true;
                })
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
                    CustomersAmenity customersAmenity = listCustomersAmenity
                            .stream()
                            .filter(j -> i.getCustomerId().equalsIgnoreCase(j.getCustomerId()))
                            .findFirst()
                            .orElse(null);
                    if (customersAmenity != null) {
                        if (customersAmenity.getEndDate() != null) {
                            isEnding.set(true);
                            endDate.set(Utils.dateToString(customersAmenity.getEndDate()));
                        }
                    }
                    Integer bedId = bedCustomerMapper.get(i.getCustomerId());
                    if (bedId != null) {
                        BedDetails bedDetails = listBeds.stream()
                                .filter(t -> t.getBedId().equals(bedId))
                                .findFirst()
                                .orElse(null);
                        if (bedDetails != null) {
                            bedName.set(bedDetails.getBedName());
                            floorName.set(bedDetails.getFloorName());
                            roomName.set(bedDetails.getRoomName());
                        }
                    }

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

                    return new CustomerResponse(i.getCustomerId(),
                            fullName.toString(),
                            initials.toString(),
                            i.getProfilePic(),
                            i.getMobile(),
                            "91",
                            bedName.get(),
                            floorName.get(),
                            roomName.get(),
                            false,
                            isEnding.get(),
                            endDate.get());
                })
                .toList();

        List<CustomerResponse> unassigned = listCustomers
                .stream()
                .filter(i -> unique.contains(i.getCustomerId()))
                .map(i -> {
                    boolean canAssign = true;

                    StringBuilder fullName = new StringBuilder();
                    StringBuilder initials = new StringBuilder();

                    Integer bedId = bedCustomerMapper.get(i.getCustomerId());
                    if (bedId != null) {
                        BedDetails bedDetails = listBeds.stream()
                                .filter(t -> t.getBedId().equals(bedId))
                                .findFirst()
                                .orElse(null);
                        if (bedDetails != null) {
                            bedName.set(bedDetails.getBedName());
                            floorName.set(bedDetails.getFloorName());
                            roomName.set(bedDetails.getRoomName());
                        }
                    }

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
                    return new CustomerResponse(i.getCustomerId(),
                            fullName.toString(),
                            initials.toString(),
                            i.getProfilePic(),
                            i.getMobile(),
                            "91",
                            bedName.get(),
                            floorName.get(),
                            roomName.get(),
                            canAssign,
                            false,
                            null);
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
