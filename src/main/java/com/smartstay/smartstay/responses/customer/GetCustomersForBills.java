package com.smartstay.smartstay.responses.customer;

public record GetCustomersForBills(String customerId,
                                   String fullName,
                                   String firstName,
                                   String lastName,
                                   String joiningDate,
                                   String status,
                                   String expectedJoiningDate,
                                   Double rent) {
}
