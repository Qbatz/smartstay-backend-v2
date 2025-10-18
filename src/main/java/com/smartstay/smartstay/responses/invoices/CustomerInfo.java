package com.smartstay.smartstay.responses.invoices;

public record CustomerInfo(String firstName,
                           String lastName,
                           String fullName,
                           String customerMobileNo,
                           String countryCode,
                           String fullAddress) {
}
