package com.smartstay.smartstay.Wrappers.retainer;

import com.smartstay.smartstay.dao.BookingsV1;
import com.smartstay.smartstay.dao.CustomerAdditionalContacts;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.customer.StayInfo;
import com.smartstay.smartstay.responses.customer.AdditionalContacts;
import com.smartstay.smartstay.responses.retainer.CustomersList;
import com.smartstay.smartstay.responses.retainer.Guardians;
import com.smartstay.smartstay.util.CustomerUtils;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CustomersListMapper implements Function<Customers, CustomersList>  {

    private List<CustomerAdditionalContacts> additionalContacts = null;
    private List<BedDetails> listBedDetails = null;
    private List<BookingsV1> listBookings = null;

    public CustomersListMapper(List<CustomerAdditionalContacts> additionalContacts, List<BookingsV1> listBookings, List<BedDetails> listBedDetails) {
        this.additionalContacts = additionalContacts;
        this.listBookings = listBookings;
        this.listBedDetails = listBedDetails;
    }

    @Override
    public CustomersList apply(Customers customers) {
        List<Guardians> guardiansList = new ArrayList<>();
        BedDetails bedDetails = null;
        StayInfo stayInfo = null;
        String joiningDate = null;

        if (listBookings != null) {
            BookingsV1 bookingsV1 = listBookings
                    .stream()
                    .filter(i -> i.getCustomerId().equalsIgnoreCase(customers.getCustomerId()))
                    .findFirst()
                    .orElse(null);
            if (bookingsV1 != null) {
                if (bookingsV1.getJoiningDate() != null) {
                    joiningDate = Utils.dateToString(bookingsV1.getJoiningDate());
                }
                if (listBedDetails != null) {
                    bedDetails = listBedDetails
                            .stream()
                            .filter(i -> i.getBedId().equals(bookingsV1.getBedId()))
                            .findFirst()
                            .orElse(null);
                    if (bedDetails != null) {
                        stayInfo = new StayInfo(bedDetails.getBedName(),
                                bedDetails.getFloorName(),
                                bedDetails.getRoomName());
                    }
                }
            }
        }
        if (additionalContacts != null) {
            List<CustomerAdditionalContacts> listAdditionalContacts = additionalContacts
                    .stream()
                    .filter(i -> i.getCustomerId().equalsIgnoreCase(customers.getCustomerId()))
                    .toList();
            if (listAdditionalContacts != null) {
                guardiansList = listAdditionalContacts
                        .stream()
                        .map(i -> new AdditionalContactsGuardianMapper().apply(i))
                        .toList();
            }
        }
        return new CustomersList(customers.getCustomerId(),
                NameUtils.getFullName(customers.getFirstName(), customers.getLastName()),
                customers.getFirstName(),
                customers.getLastName(),
                CustomerUtils.getProfilePic(customers),
                NameUtils.getInitials(customers.getFirstName(), customers.getLastName()),
                customers.getMobile(),
                "91",
                0.0,
                0.0,
                0.0,
                joiningDate,
                stayInfo,
                CustomerUtils.getCustomerAddress(customers),
                guardiansList);
    }
}
