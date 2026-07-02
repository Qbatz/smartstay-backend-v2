package com.smartstay.smartstay.responses.customer;

public record KycAddressDetails(String locality,
                                String city,
                                String state,
                                String pinCode,
                                String fullAddress) {
}
