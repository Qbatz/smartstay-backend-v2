package com.smartstay.smartstay.responses.customer;

import com.smartstay.smartstay.dto.customer.AddressInfo;
import com.smartstay.smartstay.dto.customer.StayInfo;

public record GetCustomersForBills(String customerId,
                                   String fullName,
                                   String firstName,
                                   String lastName,
                                   String joiningDate,
                                   String status,
                                   String expectedJoiningDate,
                                   Double rent,
                                   StayInfo stayInfo,
                                   AddressInfo addressInfo) {
}
