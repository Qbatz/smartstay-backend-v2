package com.smartstay.smartstay.Wrappers.Electricity;

import com.smartstay.smartstay.dto.booking.BookedCustomer;
import com.smartstay.smartstay.responses.electricity.CustomersList;
import com.smartstay.smartstay.util.Utils;

import java.util.Date;
import java.util.function.Function;

public class CustomersListMapper implements Function<BookedCustomer, CustomersList> {
    private Double totalPrice = null;
    private Double totalUnits = null;

    private Date startDate = null;
    private Date endDate = null;

    public CustomersListMapper(Double totalPrice, Double totalUnits, Date startDate, Date endDate) {
        this.totalPrice = totalPrice;
        this.totalUnits = totalUnits;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public CustomersList apply(BookedCustomer bookedCustomer) {

        long factor = (long) Math.pow(10, 2);
        totalPrice = totalPrice * factor;
        long tmp = Math.round(totalPrice);

        totalUnits = totalUnits * factor;
        long tmp2 = Math.round(totalUnits);

        StringBuilder fullName = new StringBuilder();
        if (bookedCustomer.getFirstName() != null) {
            fullName.append(bookedCustomer.getFirstName());
        }
        if (bookedCustomer.getLastName() != null && !bookedCustomer.getLastName().equalsIgnoreCase("")) {
            fullName.append(" ");
            fullName.append(bookedCustomer.getLastName());
        }
        return new CustomersList(bookedCustomer.getCustomerId(),
                bookedCustomer.getFirstName(),
                bookedCustomer.getLastName(),
                fullName.toString(),
                bookedCustomer.getFloorId(),
                bookedCustomer.getFloorName(),
                bookedCustomer.getRoomId(),
                bookedCustomer.getRoomName(),
                bookedCustomer.getBedId(),
                bookedCustomer.getBedName(),
                (double) tmp2/factor,
                (double) tmp/factor,
                Utils.dateToString(startDate),
                Utils.dateToString(endDate));
    }
}
