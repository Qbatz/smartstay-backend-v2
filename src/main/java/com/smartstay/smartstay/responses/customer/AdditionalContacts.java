package com.smartstay.smartstay.responses.customer;

public record AdditionalContacts(String fullName,
                                 String mobile,
                                 String country,
                                 String relationship,
                                 String occupation,
                                 Long contactId) {
}
