package com.smartstay.smartstay.Wrappers.customers;


import com.smartstay.smartstay.dao.BookingsV1;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.customer.AddressInfo;
import com.smartstay.smartstay.dto.customer.StayInfo;
import com.smartstay.smartstay.responses.customer.GetCustomersForBills;
import com.smartstay.smartstay.util.CustomerUtils;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class CustomerMapperForBills implements Function<Customers, GetCustomersForBills> {

    private List<BookingsV1> listBookings = null;
    private List<BedDetails> listBedDetails = null;

    public CustomerMapperForBills(List<BookingsV1> listBookings, List<BedDetails> bedDetails) {
        this.listBookings = listBookings;
        this.listBedDetails = bedDetails;
    }

    @Override
    public GetCustomersForBills apply(Customers customers) {
        String joiningDate = null;
        String expectedJoiningDate = null;
        String status = null;
        double rent = 0.0;
        StayInfo stayInfo = null;

        BookingsV1 bookingsV1 = listBookings
                .stream()
                .filter(i -> i.getCustomerId().equalsIgnoreCase(customers.getCustomerId()))
                .findFirst()
                .orElse(null);
        if (bookingsV1 != null) {
           if (bookingsV1.getJoiningDate() != null) {
               joiningDate = Utils.dateToString(bookingsV1.getJoiningDate());
           }
           if (bookingsV1.getExpectedJoiningDate() != null) {
               expectedJoiningDate = Utils.dateToString(bookingsV1.getExpectedJoiningDate());
           }
           if (bookingsV1.getRentAmount() != null) {
               rent = bookingsV1.getRentAmount();
           }

           if (listBedDetails != null) {
               BedDetails bedDetails = listBedDetails
                       .stream()
                       .filter(i -> i.getBedId().equals(bookingsV1.getBedId()))
                       .findFirst()
                       .orElse(null);
               if (bedDetails != null) {
                   stayInfo = new StayInfo(bedDetails.getBedName(), bedDetails.getFloorName(), bedDetails.getRoomName());
               }
           }
        }
        return new GetCustomersForBills(customers.getCustomerId(),
                NameUtils.getFullName(customers.getFirstName(), customers.getLastName()),
                customers.getFirstName(),
                customers.getLastName(),
                joiningDate,
                status,
                expectedJoiningDate,
                rent,
                stayInfo,
                CustomerUtils.getCustomerAddress(customers));
    }
}
