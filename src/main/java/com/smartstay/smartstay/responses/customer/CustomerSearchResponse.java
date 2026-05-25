package com.smartstay.smartstay.responses.customer;

public record CustomerSearchResponse(String customerId,
                                     String fullName,
                                     String firstName,
                                     String lastName,
                                     String profilePic,
                                     String initials,
                                     String mobile,
                                     String emailId) {
}
