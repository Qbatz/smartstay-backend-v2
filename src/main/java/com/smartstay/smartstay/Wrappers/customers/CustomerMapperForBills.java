package com.smartstay.smartstay.Wrappers.customers;


import com.smartstay.smartstay.dao.BookingsV1;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.responses.customer.GetCustomersForBills;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class CustomerMapperForBills implements Function<Customers, GetCustomersForBills> {

    private List<BookingsV1> listBookings = null;

    public CustomerMapperForBills(List<BookingsV1> listBookings) {
        this.listBookings = listBookings;
    }

    @Override
    public GetCustomersForBills apply(Customers customers) {
        String joiningDate = null;
        String expectedJoiningDate = null;
        String status = null;
        double rent = 0.0;

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
           rent = bookingsV1.getRentAmount();
        }
        return new GetCustomersForBills(customers.getCustomerId(),
                NameUtils.getFullName(customers.getFirstName(), customers.getLastName()),
                customers.getFirstName(),
                customers.getLastName(),
                joiningDate,
                status,
                expectedJoiningDate,
                rent);
    }
}
