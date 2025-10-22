package com.smartstay.smartstay.Wrappers.Electricity;

import com.smartstay.smartstay.dao.CustomersBedHistory;
import com.smartstay.smartstay.dto.electricity.CustomerBedsList;

import java.util.function.Function;

public class BedHistoryCustomerListMapper implements Function<CustomersBedHistory, CustomerBedsList> {
    @Override
    public CustomerBedsList apply(CustomersBedHistory customersBedHistory) {
        return new CustomerBedsList(customersBedHistory.getCustomerId(),
                customersBedHistory.getBedId(),
                customersBedHistory.getRoomId(),
                customersBedHistory.getStartDate(),
                customersBedHistory.getEndDate());
    }
}
