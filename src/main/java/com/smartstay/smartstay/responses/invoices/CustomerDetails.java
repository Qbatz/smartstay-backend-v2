package com.smartstay.smartstay.responses.invoices;

public record CustomerDetails(String fullName,
                              String firstName,
                              String lastName,
                              String initials,
                              String profilePic,
                              String customerId) {
}
