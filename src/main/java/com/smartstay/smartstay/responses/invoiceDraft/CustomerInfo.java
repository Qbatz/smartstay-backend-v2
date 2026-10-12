package com.smartstay.smartstay.responses.invoiceDraft;

import java.util.List;

public record CustomerInfo(String firstName,
                           String lastName,
                           String fullName,
                           String profilePic,
                           String initials,
                           String customerId,
                           String mobile,
                           String countryCode) {
}
