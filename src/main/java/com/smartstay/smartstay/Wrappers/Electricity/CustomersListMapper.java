package com.smartstay.smartstay.Wrappers.Electricity;

import com.smartstay.smartstay.dto.booking.BookedCustomer;
import com.smartstay.smartstay.dto.electricity.ElectricityCustomersList;
import com.smartstay.smartstay.responses.electricity.CustomersList;
import com.smartstay.smartstay.util.Utils;

import java.util.Date;
import java.util.function.Function;

public class CustomersListMapper implements Function<ElectricityCustomersList, CustomersList> {

    @Override
    public CustomersList apply(ElectricityCustomersList customersList) {


        StringBuilder fullName = new StringBuilder();
        StringBuilder initials = new StringBuilder();
        if (customersList.getFirstName() != null) {
            fullName.append(customersList.getFirstName());
            initials.append(customersList.getFirstName().toUpperCase().charAt(0));
        }
        if (customersList.getLastName() != null && !customersList.getLastName().equalsIgnoreCase("")) {
            fullName.append(" ");
            fullName.append(customersList.getLastName());
            initials.append(customersList.getLastName().toUpperCase().charAt(0));
        }
        else {
            if (customersList.getFirstName().length() > 1) {
                initials.append(customersList.getFirstName().toUpperCase().charAt(1));
            }
        }
        return new CustomersList(customersList.getCustomerId(),
                customersList.getFirstName(),
                customersList.getLastName(),
                fullName.toString(),
                initials.toString(),
                customersList.getProfilePic(),
                customersList.getFloorId(),
                customersList.getFloorName(),
                customersList.getRoomId(),
                customersList.getRoomName(),
                customersList.getBedId(),
                customersList.getBedName(),
                Math.round(customersList.getConsumption() * 100.0)/100.0,
                Math.round(customersList.getAmount() * 100.0) / 100.0,
                Utils.dateToString(customersList.getStartDate()),
                Utils.dateToString(customersList.getEndDate()));
    }
}
