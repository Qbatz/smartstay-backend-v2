package com.smartstay.smartstay.responses.customer;

public record WalkInCustomers(String customerId,
                              String fullName,
                              String profilePic,
                              String initials,
                              String mobile,
                              String emailId) {
}
